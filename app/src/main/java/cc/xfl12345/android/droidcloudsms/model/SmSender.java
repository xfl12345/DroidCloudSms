package cc.xfl12345.android.droidcloudsms.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.RemoteException;

import java.io.Closeable;
import java.io.IOException;

public class SmSender implements Closeable {
    public static final String NOTIFICATION_TITLE = "短信服务";

    public static final String SENT_SMS_ACTION = "cc.xfl12345.android.droidcloudsms.action.SENT_SMS_ACTION";

    protected int sequence = 1;

    protected final MySmsManager smsManager;

    protected Context context;

    protected BroadcastReceiver broadcastReceiver;

    public MySmsManager getSmsManager() {
        return smsManager;
    }

    public SmSender(Context context) throws ReflectiveOperationException, RemoteException {
        this.context = context;
        smsManager = new MySmsManager();

        registerReceiver();
    }

    /**
     * 发送短信
     *
     * @param content 发送内容
     * @param phoneNumber 电话号码
     */
    public void sendMessage(String content, String phoneNumber) {
        Intent sentIntent = new Intent(SENT_SMS_ACTION);

        int currentSequence = 0;
        synchronized (SENT_SMS_ACTION) {
            currentSequence = sequence;
            sequence += 1;
        }
        sentIntent.putExtra("sequence", currentSequence);
        sentIntent.putExtra("phoneNumber", phoneNumber);
        sentIntent.putExtra("content", content);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, currentSequence, sentIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        smsManager.sendTextMessage(phoneNumber, null, content, sentPI, null);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    protected void registerReceiver() {
        IntentFilter filter = new IntentFilter(SmSender.SENT_SMS_ACTION);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int sequence = intent.getIntExtra("sequence", -1);
                String phoneNumber = intent.getStringExtra("phoneNumber");
                String smsContent = intent.getStringExtra("content");

                String notificationContent = (getResultCode() == Activity.RESULT_OK ?
                    "第" + sequence + "条 发送成功！" :
                    "第" + sequence + "条 发送失败，状态码：[" + getResultCode() + "]，" ) +
                    "收件人：" + phoneNumber + ", 内容：[" + smsContent + "]";
                NotificationUtils.postNotification(context, NOTIFICATION_TITLE, notificationContent);
                // case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                // case SmsManager.RESULT_ERROR_RADIO_OFF:
                // case SmsManager.RESULT_ERROR_NULL_PDU:
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            context.registerReceiver(broadcastReceiver, filter);
        }
    }

    protected void unregisterReceiver() {
        context.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void close() throws IOException {
        unregisterReceiver();
    }

}
