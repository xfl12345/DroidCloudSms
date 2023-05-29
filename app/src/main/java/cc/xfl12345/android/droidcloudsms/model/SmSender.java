package cc.xfl12345.android.droidcloudsms.model;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
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

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.Closeable;
import java.io.IOException;

import cc.xfl12345.android.droidcloudsms.R;

public class SmSender implements Closeable {
    public static final String CHANNEL_ID = "SmSender";

    public static final String CHANNEL_NAME = "短信服务";

    public static final String NOTIFICATION_TITLE = "短信服务";

    public static final String SENT_SM_ACTION = "cc.xfl12345.android.droidcloudsms.SmSender.SENT_SM_ACTION";
    public static final String CLEAR_NOTIFICATION_ACTION = "cc.xfl12345.android.droidcloudsms.SmSender.CLEAR_NOTIFICATION_ACTION";

    protected int sequence = 1;

    protected final MySmsManager smsManager;

    protected Context context;

    protected BroadcastReceiver sentSmActionBroadcastReceiver;

    protected BroadcastReceiver clearNotificationActionBroadcastReceiver;

    public MySmsManager getSmsManager() {
        return smsManager;
    }

    public SmSender(Context context) throws ReflectiveOperationException, RemoteException {
        this.context = context;
        smsManager = new MySmsManager();

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
     *
     * @param phoneNumber 电话号码
     * @param content     发送内容
     */
    public void sendMessage(String phoneNumber, String content) {
        Intent sentIntent = new Intent(SENT_SM_ACTION);

        int currentSequence = 0;
        synchronized (SENT_SM_ACTION) {
            currentSequence = sequence;
            sequence += 1;
        }

        sentIntent.putExtra("sequence", currentSequence);
        sentIntent.putExtra("phoneNumber", phoneNumber);
        sentIntent.putExtra("content", content);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, currentSequence, sentIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        smsManager.sendTextMessage(phoneNumber, null, content, sentPI, null);
    }

    protected void registerReceiver() {
        sentSmActionBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int sequence = intent.getIntExtra("sequence", -1);
                String phoneNumber = intent.getStringExtra("phoneNumber");
                String smContent = intent.getStringExtra("content");

                // case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                // case SmsManager.RESULT_ERROR_RADIO_OFF:
                // case SmsManager.RESULT_ERROR_NULL_PDU:
                // SmsManager.RESULT_ERROR_GENERIC_FAILURE
                String notificationContent = (getResultCode() == Activity.RESULT_OK ?
                    "第" + sequence + "条 发送成功！" :
                    "第" + sequence + "条 发送失败，状态码：[" + getResultCode() + "]，") +
                    "收件人：" + phoneNumber + ", 内容：[" + smContent + "]";

                Intent clearNotificationIntent = new Intent(CLEAR_NOTIFICATION_ACTION);
                clearNotificationIntent.putExtra("sequence", sequence);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    sequence,
                    clearNotificationIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT
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
            }
        };

        clearNotificationActionBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int sequence = intent.getIntExtra("sequence", -1);
                if (sequence != -1) {
                    getNotificationManager().cancel(CHANNEL_ID, sequence);
                }
            }
        };


        ContextCompat.registerReceiver(
            context,
            sentSmActionBroadcastReceiver,
            new IntentFilter(SENT_SM_ACTION),
            ContextCompat.RECEIVER_EXPORTED
        );
        ContextCompat.registerReceiver(
            context,
            clearNotificationActionBroadcastReceiver,
            new IntentFilter(CLEAR_NOTIFICATION_ACTION),
            ContextCompat.RECEIVER_EXPORTED
        );

    }

    protected void unregisterReceiver() {
        context.unregisterReceiver(sentSmActionBroadcastReceiver);
        context.unregisterReceiver(clearNotificationActionBroadcastReceiver);
    }

    @Override
    public void close() throws IOException {
        unregisterReceiver();
    }

}
