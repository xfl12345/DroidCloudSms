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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import cc.xfl12345.android.droidcloudsms.model.MyShizukuContext;
import cc.xfl12345.android.droidcloudsms.model.NotificationUtils;
import cc.xfl12345.android.droidcloudsms.model.SmSender;

public class MyApplication extends Application {
    public static final int STALE_NOTIFICATION_ID = 0;

    private Context context;

    private MyShizukuContext myShizukuContext;

    public MyShizukuContext getMyShizukuContext() {
        return myShizukuContext;
    }

    private SmSender smSender = null;

    public SmSender getSmSender() {
        if (smSender == null) {
            if (myShizukuContext.requirePermission()) {
                try {
                    smSender = new SmSender(context);
                } catch (ReflectiveOperationException | RemoteException e) {
                    NotificationUtils.postNotification(context, SmSender.NOTIFICATION_TITLE, "创建短信服务失败！原因：" + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                NotificationUtils.postNotification(context, SmSender.NOTIFICATION_TITLE, "创建短信服务失败！原因：" + "Shizuku 未授权");
            }
        }

        return smSender;
    }

    private NotificationManager notificationManager;

    public static List<Map.Entry<String, String>> permissionlist = new ArrayList<>();

    private Intent foregroundServiceIntent;

    private ServiceConnection foregroundServiceConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        myShizukuContext = new MyShizukuContext(context);

        permissionlist = new ArrayList<>(3);
        permissionlist.add(Map.entry(Permission.NOTIFICATION_SERVICE, "通知栏权限"));
        permissionlist.add(Map.entry(Permission.POST_NOTIFICATIONS, "发送通知权限"));
        permissionlist.add(Map.entry(Permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "忽略电池优化选项权限"));

        // 注册通用通知回调
        NotificationUtils.registerReceiver(context);

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
                MyActivityManager.addActivity(activity);
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
                MyActivityManager.removeActivity(activity);
            }
        });

        // 吊起前台保活服务
        foregroundServiceIntent = new Intent().setClass(getApplicationContext(), ForegroundService.class);
        foregroundServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        bindService(foregroundServiceIntent, foregroundServiceConnection, Context.BIND_IMPORTANT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(foregroundServiceIntent);
        } else {
            startService(foregroundServiceIntent);
        }
    }

    public void justExit() {
        onTerminate();
        // System.exit(0);
    }

    @Override
    public void onTerminate() {
        notificationManager.cancel(STALE_NOTIFICATION_ID);
        MyActivityManager.finishAllActivity();
        unbindService(foregroundServiceConnection);
        stopService(foregroundServiceIntent);
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

    public boolean isAllPermissionGranted() {
        return XXPermissions.isGranted(context, MyApplication.permissionlist.stream().map(Map.Entry::getKey).collect(Collectors.toList()));
    }

    public static Activity getCurrentActivity() {
        return MyActivityManager.getCurrentActivity();
    }


    private static class MyActivityManager {
        private static WeakReference<Activity> currentActivity = null;

        public static void setCurrentActivity(Activity activity) {
            currentActivity = new WeakReference<>(activity);
        }

        public static Activity getCurrentActivity() {
            return currentActivity == null ? null : currentActivity.get();
        }

        private static final List<Activity> activityStack = new CopyOnWriteArrayList<>();

        public static void addActivity(Activity activity) {
            activityStack.add(activity);
        }

        public static void removeActivity(Activity activity) {
            activityStack.remove(activity);
        }

        /**
         * 结束所有Activity
         * <a herf="https://blog.csdn.net/lfq88/article/details/126742032">source code URL</a>
         */
        public static void finishAllActivity() {
            for (int i = 0, size = activityStack.size(); i < size; i++) {
                if (null != activityStack.get(i)) {
                    activityStack.get(i).finish();
                }
            }

            activityStack.clear();
        }
    }
}
