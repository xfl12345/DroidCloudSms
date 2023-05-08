package cc.xfl12345.android.droidcloudsms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SmsSender {
    public static final String SENT_SMS_ACTION = "cc.xfl12345.android.xposed.mysmssender.action.SENT_SMS_ACTION";

    private MySmsManager manager;

    protected Context context;

    public SmsSender(Context context) throws ReflectiveOperationException, RemoteException {
        this.context = context;
        manager = new MySmsManager();

        registerReceiver();
    }

    /**
     * 发送短信
     *
     * @param content 发送内容
     * @param phoneNumber 电话号码
     * @param code 独一无二的请求码（用以广播接收）
     */
    public void sendMessage(String content, String phoneNumber, int code) {
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        sentIntent.putExtra("code", code);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, code, sentIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        manager.sendTextMessage(phoneNumber, null, content, sentPI, null);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(SmsSender.SENT_SMS_ACTION);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int code = intent.getIntExtra("code", -1);
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "第" + code + "条 发送成功\n", Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "第" + code + "条 发送失败\n", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }, filter);
    }
}
