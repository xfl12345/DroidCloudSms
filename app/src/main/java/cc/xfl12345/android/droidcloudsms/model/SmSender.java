package cc.xfl12345.android.droidcloudsms.model;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;

public class SmSender {
    public static final String NOTIFICATION_TITLE = "短信服务";

    public static final String SENT_SMS_ACTION = "cc.xfl12345.android.xposed.mysmssender.action.SENT_SMS_ACTION";

    private int sequence = 1;

    private final MySmsManager smsManager;

    protected Context context;

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
        PendingIntent sentPI = PendingIntent.getBroadcast(context, currentSequence, sentIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        smsManager.sendTextMessage(phoneNumber, null, content, sentPI, null);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(SmSender.SENT_SMS_ACTION);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int sequence = intent.getIntExtra("sequence", -1);
                NotificationUtils.postNotification(
                    context,
                    NOTIFICATION_TITLE,
                    getResultCode() == Activity.RESULT_OK ?
                        "第" + sequence + "条 发送成功" :
                        "第" + sequence + "条 发送失败，状态码：" + getResultCode()
                );
                // case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                // case SmsManager.RESULT_ERROR_RADIO_OFF:
                // case SmsManager.RESULT_ERROR_NULL_PDU:
            }
        }, filter);
    }
}
