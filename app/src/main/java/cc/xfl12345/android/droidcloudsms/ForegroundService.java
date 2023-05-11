package cc.xfl12345.android.droidcloudsms;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

// source code URL=https://blog.csdn.net/yxl930401/article/details/127963284
// source code URL=https://blog.csdn.net/weixin_35691921/article/details/124419935
public class ForegroundService extends Service {
    private static final String TAG = "DroidCloudSmsKeepAliveService";

    public static final String SERVICE_NAME = TAG;

    public static final String SERVICE_NOTIFICATION_NAME = "SmsWebsocket";

    public static final int SERVICE_ID_INT = 1;


    private IBinder binder;


    public ForegroundService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate...");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // 4.3以下
            startForeground(SERVICE_ID_INT, new Notification());
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // 7.0以下
            startForeground(SERVICE_ID_INT, new Notification());
        } else {
            // 8.0以上
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                // NotificationManager.IMPORTANCE_MIN 通知栏消息的重要级别  最低，不让弹出
                // IMPORTANCE_MIN 前台时，在阴影区能看到，后台时 阴影区不消失，增加显示 IMPORTANCE_NONE时 一样的提示
                // IMPORTANCE_NONE app在前台没有通知显示，后台时有
                NotificationChannel channel = new NotificationChannel(SERVICE_NAME, SERVICE_NOTIFICATION_NAME, NotificationManager.IMPORTANCE_NONE);
                notificationManager.createNotificationChannel(channel);
                Notification notification = new Notification.Builder(this, SERVICE_NAME).setOngoing(true).build();
                startForeground(SERVICE_ID_INT, notification);
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy...");
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
    }



    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind...");
        // // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        binder = new Binder();
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.i(TAG, "onRebind...");
    }

}
