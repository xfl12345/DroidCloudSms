package cc.xfl12345.android.droidcloudsms;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import cc.xfl12345.android.droidcloudsms.model.NotificationUtils;
import cc.xfl12345.android.droidcloudsms.model.MyActivityManager;

public class MyApplication extends Application {
    public static final int STALE_NOTIFICATION_ID = 0;

    private Context context;

    private AnyLauncherMain anyLauncherMain;

    public AnyLauncherMain getAnyLaucherMain() {
        return anyLauncherMain;
    }

    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {   // 版本大于等于 安卓8.0
            NotificationChannel channel = new NotificationChannel(
                NotificationUtils.CHANNEL_ID,
                NotificationUtils.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        createStaleNotification();

        // source code URL=https://www.jianshu.com/p/b245c4e56e6c
        // 监听所有Activity的生命周期回调
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                // 在此处设置当前的Activity
                MyActivityManager.setCurrentActivity(activity);
                recreateStaleNotification();
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });

        anyLauncherMain = new AnyLauncherMain(getApplicationContext());

        // 吊起前台保活服务
        Intent intent = new Intent().setClass(getApplicationContext(), ForegroundService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        }, Context.BIND_AUTO_CREATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void onTerminate() {

        super.onTerminate();
    }

    private void createStaleNotification() {
        Intent appIntent = new Intent(Intent.ACTION_MAIN);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        Activity activity = MyActivityManager.getCurrentActivity();
        appIntent.setComponent(new ComponentName(
            context.getPackageName(),
            context.getPackageName() + (activity == null ? "" : "." + activity.getLocalClassName())
        ));
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(context, NotificationUtils.CHANNEL_ID)
            .setSmallIcon(R.drawable.miyamizu_mitsuha_head)
            // .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.miyamizu_mitsuha_head))  //设置大图标
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setGroupSummary(false)
            .setGroup(context.getString(R.string.app_name))
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("点我可返回APP。因为已编程为“不显示在近期任务”，所以快速返回入口放到通知栏里了。")
            .setAutoCancel(false)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true) // 通知栏常驻
            .build();
        notificationManager.notify(STALE_NOTIFICATION_ID, notification);
    }

    private void recreateStaleNotification() {
        notificationManager.cancel(STALE_NOTIFICATION_ID);
        createStaleNotification();
    }
}
