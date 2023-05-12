package cc.xfl12345.android.droidcloudsms.model;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import cc.xfl12345.android.droidcloudsms.R;

public class NotificationUtils {
    public static final String CLEAR_COMMON_NOTIFICATION_ACTION = "cc.xfl12345.android.droidcloudsms.model.NotificationUtils.CLEAR_COMMON_NOTIFICATION_ACTION";

    public static final String CHANNEL_ID = "commonNotification";

    public static final String CHANNEL_NAME = "通用通知";

    private static final IdGenerator notificationIdGenerator = new IdGenerator(1);

    public static NotificationManager getNotificationManager(Context context) {
        return ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE));
    }

    public static int postNotification(Context context, String title, String content){
        int requestCode = notificationIdGenerator.generate();

        // 设置取消后的动作
        Intent intent = new Intent(CLEAR_COMMON_NOTIFICATION_ACTION);
        intent.putExtra("sequence", requestCode);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        //初始化 notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)    //设置标题
            .setContentText(content)    //设置通知文字
            .setSmallIcon(R.drawable.baseline_contact_mail_24)   //设置左边的小图标
            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.miyamizu_mitsuha_head))  //设置大图标
            // .setColor(Color.parseColor("#ff0000"))
            .setContentIntent(pendingIntent)  // 设置点击通知之后 进入相关页面(此处进入NotificationActivity类，执行oncreat方法打印日志)
            .setOngoing(false)
            .setAutoCancel(true)   //设置点击通知后 通知通知栏不显示 （但实测不行，目前使用 pendingIntent 回调来删除通知）
            .build();

        getNotificationManager(context).notify(CHANNEL_ID, requestCode, notification);

        return requestCode;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public static void registerReceiver(Context context) {
        IntentFilter filter = new IntentFilter(NotificationUtils.CLEAR_COMMON_NOTIFICATION_ACTION);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int sequence = intent.getIntExtra("sequence", -1);
                if (sequence != -1) {
                    getNotificationManager(context).cancel(CHANNEL_ID, sequence);
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            context.registerReceiver(broadcastReceiver, filter);
        }
    }
}
