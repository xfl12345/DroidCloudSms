// package cc.xfl12345.android.droidcloudsms.model;
//
// import static android.content.Context.NOTIFICATION_SERVICE;
//
// import android.app.Notification;
// import android.app.NotificationChannel;
// import android.app.NotificationManager;
// import android.app.PendingIntent;
// import android.content.Context;
// import android.content.Intent;
// import android.graphics.BitmapFactory;
// import android.graphics.Color;
// import android.os.Build;
//
// import androidx.core.app.NotificationCompat;
//
// import cc.xfl12345.android.droidcloudsms.R;
// import cc.xfl12345.android.droidcloudsms.ui.MainActivity;
//
// public class MyNotification {
//     protected final IdGenerator notificationIdGenerator = new IdGenerator(1);
//
//     protected NotificationManager notificationManager;
//
//     protected final Context context;
//
//     private final String channelId;
//
//     public String getChannelId() {
//         return channelId;
//     }
//
//     public MyNotification(Context context, String channelId) {
//         this.context = context;
//         this.channelId = channelId;
//
//         notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
//
//         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){   //版本大于等于 安卓8.0
//             NotificationChannel channel = new NotificationChannel("anyNotification", "通用通知", NotificationManager.IMPORTANCE_HIGH);
//             notificationManager.createNotificationChannel(channel);
//         }
//     }
//
//
//     public static int postNotification(Context context, String title, String content){
//         int requestCode = notificationIdGenerator.generate();
//
//         // 设置取消后的动作
//         Intent intent = new Intent();
//         intent.setClass(context, MainActivity.class);
//         PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
//
//         //初始化 notification
//         Notification notification = new NotificationCompat.Builder(context, "xfl")
//             .setContentTitle(title)    //设置标题
//             .setContentText(content)    //设置通知文字
//             .setSmallIcon(R.drawable.ic_launcher_foreground)   //设置左边的小图标
//             .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_foreground))  //设置大图标
//             .setColor(Color.parseColor("#ff0000"))
//             .setContentIntent(pendingIntent)  // 设置点击通知之后 进入相关页面(此处进入NotificationActivity类，执行oncreat方法打印日志)
//             .setAutoCancel(true)   //设置点击通知后 通知通知栏不显示
//             .build();
//
//         ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).notify(requestCode, notification);
//
//         return requestCode;
//     }
// }
