package cc.xfl12345.android.droidcloudsms.model;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.teasoft.bee.osql.IncludeType;
import org.teasoft.honey.osql.core.ConditionImpl;
import org.teasoft.honey.osql.shortcut.BF;

import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.model.database.SmsLog;
import cc.xfl12345.android.droidcloudsms.model.ws.SmsTask;
import jakarta.annotation.PreDestroy;

public class SmSender {
    public static final String CHANNEL_ID = "SmSender";

    public static final String CHANNEL_NAME = "短信服务";

    public static final String NOTIFICATION_TITLE = "短信服务";

    public static final String SENT_SM_ACTION = "cc.xfl12345.android.droidcloudsms.SmSender.SENT_SM_ACTION";

    private final IdGenerator notificationIdGenerator = new IdGenerator(1);

    protected final MySmsManager smsManager;

    protected Context context;

    protected BroadcastReceiver sentSmActionBroadcastReceiver;

    public MySmsManager getSmsManager() {
        return smsManager;
    }

    public SmSender(Context context) throws ReflectiveOperationException, RemoteException {
        this.context = context;
        smsManager = new MySmsManager(context);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getNotificationManager();
            if (notificationManager != null) {
                NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
                if (channel == null) {
                    channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    );
                    // 启用震动
                    channel.enableVibration(true);
                    // 渐进式震动（先震动 0.8 秒，然后停止 0.5 秒，再震动 1.2 秒）
                    channel.setVibrationPattern(new long[]{800, 500, 1200});
                    notificationManager.createNotificationChannel(channel);
                }
            }
        }

        registerReceiver();
    }

    protected NotificationManager getNotificationManager() {
        return (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }


    /**
     * 发送短信
     */
    public void sendMessage(SmsTask smsTask) {
        String phoneNumber = smsTask.getPhoneNumber();
        String validationCode = smsTask.getValidationCode();
        String content = smsTask.getSmsContent();

        SmsLog smsLog = new SmsLog();
        smsLog.setSmsTask(smsTask);
        long id = 0;
        int sequence = notificationIdGenerator.generate();
        try {
            MySqliteLockManager.lockWrite();
            id = BF.getSuidRich().insertAndReturnId(smsLog);
        } catch (Exception e) {
            postNotification(sequence, "保存短信失败！", e);
        } finally {
            MySqliteLockManager.unlockWrite();
        }

        if (id == 0) {
            sequence = notificationIdGenerator.generate();
        }
        Intent sentIntent = new Intent(SENT_SM_ACTION);
        sentIntent.putExtra("dbId", id);
        sentIntent.putExtra("sequence", sequence);
        sentIntent.putExtra("phoneNumber", phoneNumber);
        sentIntent.putExtra("validationCode", validationCode);
        sentIntent.putExtra("content", content);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, sequence, sentIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        smsManager.sendTextMessage(phoneNumber, null, content, sentPI, null);
    }

    protected void registerReceiver() {
        sentSmActionBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int sequence = intent.getIntExtra("sequence", -1);
                long id = intent.getLongExtra("dbId", -1);
                String phoneNumber = intent.getStringExtra("phoneNumber");
                String smContent = intent.getStringExtra("content");

                // case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                // case SmsManager.RESULT_ERROR_RADIO_OFF:
                // case SmsManager.RESULT_ERROR_NULL_PDU:
                // SmsManager.RESULT_ERROR_GENERIC_FAILURE
                // SmsManager.RESULT_
                int resultCode = getResultCode();
                boolean isSuccess = resultCode == Activity.RESULT_OK;
                String notificationContent = (isSuccess
                    ? "ID:" + id + "，发送成功！"
                    : String.format("ID:%s，发送失败！状态码：[%s]，", id, resultCode))
                    + String.format("收件人：%s，内容：[%s]", phoneNumber, smContent);

                PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    sequence,
                    new Intent(),
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
                );
                Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle(NOTIFICATION_TITLE)
                    .setContentText(notificationContent)
                    .setSmallIcon(R.drawable.baseline_contact_mail_24)
                    .setContentIntent(pendingIntent)
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .build();
                getNotificationManager().notify(CHANNEL_ID, sequence, notification);

                if (id > 0) {
                    try {
                        MySqliteLockManager.lockWrite();
                        SmsLog smsLog = new SmsLog();
                        smsLog.setId(id);
                        smsLog.setSmsResultCode(resultCode);
                        BF.getSuidRich().updateById(smsLog, new ConditionImpl().setIncludeType(IncludeType.EXCLUDE_BOTH));
                    } catch (Exception e) {
                        postNotification(notificationIdGenerator.generate(), "更新短信失败！", e);
                    } finally {
                        MySqliteLockManager.unlockWrite();
                    }
                }
            }
        };


        ContextCompat.registerReceiver(
            context,
            sentSmActionBroadcastReceiver,
            new IntentFilter(SENT_SM_ACTION),
            ContextCompat.RECEIVER_EXPORTED
        );
    }

    protected void postNotification(int sequence, String content, Exception e) {
        Log.e(SmSender.class.getCanonicalName(), e.getMessage(), e);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(content + "调试信息：" + e.getMessage())
            .setSmallIcon(R.drawable.baseline_contact_mail_24)
            .setContentIntent(PendingIntent.getActivity(
                context,
                sequence,
                new Intent(),
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
            ))
            .setOngoing(false)
            .setAutoCancel(true)
            .build();
        getNotificationManager().notify(
            CHANNEL_ID,
            sequence,
            notification
        );
    }

    protected void unregisterReceiver() {
        context.unregisterReceiver(sentSmActionBroadcastReceiver);
    }

    @PreDestroy
    public void destroy() {
        unregisterReceiver();
        smsManager.destroy();
    }

}
