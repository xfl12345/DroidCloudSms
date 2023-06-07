package cc.xfl12345.android.droidcloudsms.model;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import cc.xfl12345.android.droidcloudsms.R;

public class NotificationUtils {
    public static final String CHANNEL_ID = "commonNotification";

    public static final String CHANNEL_NAME = "通用通知";

    private static final IdGenerator notificationIdGenerator = new IdGenerator(1);

    private static NotificationManager getNotificationManager(Context context) {
        return ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE));
    }

    public static int postNotification(Context context, String title, String content) {
        int requestCode = notificationIdGenerator.generate();

        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, new Intent(), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        // 初始化 notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)    // 设置标题
            .setContentText(content)    // 设置通知文字
            .setSmallIcon(R.drawable.baseline_contact_mail_24)   // 设置左边的小图标
            // .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.miyamizu_mitsuha_head))  //设置大图标
            // .setColor(Color.parseColor("#ff0000"))
            .setContentIntent(pendingIntent)  // 设置点击通知之后 进入相关页面(此处进入NotificationActivity类，执行oncreat方法打印日志)
            .setOngoing(false)
            .setAutoCancel(true)   // 设置点击通知后 通知通知栏不显示 （但实测不行，目前使用 pendingIntent 回调来删除通知）
            .build();

        getNotificationManager(context).notify(CHANNEL_ID, requestCode, notification);

        return requestCode;
    }

    public static void registerNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {   // 版本大于等于 安卓8.0
            NotificationManager notificationManager = getNotificationManager(context);
            NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (channel == null) {
                channel = new NotificationChannel(
                    NotificationUtils.CHANNEL_ID,
                    NotificationUtils.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                );
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
