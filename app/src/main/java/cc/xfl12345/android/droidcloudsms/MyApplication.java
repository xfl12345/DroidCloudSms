package cc.xfl12345.android.droidcloudsms;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hjq.permissions.Permission;

import org.teasoft.beex.android.ApplicationRegistry;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cc.xfl12345.android.droidcloudsms.model.AndroidPermissionNamePair;
import cc.xfl12345.android.droidcloudsms.model.MyDatabaseHelper;
import cc.xfl12345.android.droidcloudsms.model.MyShizukuContext;
import cc.xfl12345.android.droidcloudsms.model.NotificationUtils;
import cc.xfl12345.android.droidcloudsms.model.PermissionManager;
import cc.xfl12345.android.droidcloudsms.model.WebSocketServiceConnectionEventHelper;
import cc.xfl12345.android.droidcloudsms.model.WebSocketServiceConnectionListener;

public class MyApplication extends Application {
    public static final Integer STALE_NOTIFICATION_ID = 0;

    public static final String SP_KEY_WEBSOCKET_SERVER_LOGIN_URL = "websocketServerLoginURL";

    public static final String SP_KEY_WEBSOCKET_SERVER_ACCESS_KEY_SECRET = "websocketServerAccessKeySecret";

    public static final String SP_KEY_SMS_SIM_SUBSCRIPTION_ID = "smsSimSubscriptionId";


    private Context context;

    private MyShizukuContext myShizukuContext;

    public MyShizukuContext getMyShizukuContext() {
        return myShizukuContext;
    }

    private PermissionManager permissionManager = null;

    private NotificationManager notificationManager;

    public static List<AndroidPermissionNamePair> androidPermissionList = new ArrayList<>();

    private Intent websocketServiceIntent;

    private ServiceConnection websocketServiceConnection;

    private boolean boundWebsocketService = false;

    private final WebSocketServiceConnectionEventHelper webSocketServiceConnectionEventHelper = new WebSocketServiceConnectionEventHelper();

    // public boolean isConnected2WebsocketService() {
    //     return webSocketServiceConnectionEventHelper.isConnected();
    // }
    //
    // public WebsocketService getWebsocketService() {
    //     return webSocketServiceConnectionEventHelper.getService();
    // }

    private Boolean isExiting = false;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        synchronized (STALE_NOTIFICATION_ID) {
            if (isExiting) {
                System.exit(0);
                return;
            }
        }

        myShizukuContext = new MyShizukuContext(context);

        androidPermissionList = new ArrayList<>(10);
        androidPermissionList.add(new AndroidPermissionNamePair(Manifest.permission.READ_PHONE_STATE, "获取SIM卡状态权限"));
        androidPermissionList.add(new AndroidPermissionNamePair(Manifest.permission.ACCESS_NETWORK_STATE, "获取网络状态权限"));
        androidPermissionList.add(new AndroidPermissionNamePair(Manifest.permission.INTERNET, "联网权限"));
        androidPermissionList.add(new AndroidPermissionNamePair(Manifest.permission.VIBRATE, "震动权限"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            androidPermissionList.add(new AndroidPermissionNamePair(Manifest.permission.FOREGROUND_SERVICE, "前台服务权限"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidPermissionList.add(new AndroidPermissionNamePair(Manifest.permission.POST_NOTIFICATIONS, "发送通知权限"));
        }
        androidPermissionList.add(new AndroidPermissionNamePair(Permission.NOTIFICATION_SERVICE, "通知栏权限"));
        androidPermissionList.add(new AndroidPermissionNamePair(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "忽略电池优化权限"));

        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // 注册通用通知
        NotificationUtils.registerNotification(context);
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


        new Thread(() -> {
            // 初始化数据库
            try {
                MyDatabaseHelper helper = new MyDatabaseHelper(context, "droid_cloud_sms.db", null, 1);
                helper.getWritableDatabase().close();
                helper.close();
            } catch (Exception e) {
                Log.d(MyApplication.class.getCanonicalName(), "创建数据库失败", e);
            }
            ApplicationRegistry.register(this);//注册上下文

            // 吊起前台保活服务
            websocketServiceIntent = new Intent().setClass(getApplicationContext(), WebsocketService.class);
            websocketServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder iBinder) {
                    webSocketServiceConnectionEventHelper.onServiceConnected(((WebsocketService.WebsocketServiceBinder) iBinder).getService());
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    webSocketServiceConnectionEventHelper.onServiceDisconnected();
                }
            };
            boundWebsocketService = bindService(websocketServiceIntent, websocketServiceConnection, Context.BIND_AUTO_CREATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(websocketServiceIntent);
            } else {
                startService(websocketServiceIntent);
            }
        }, MyApplication.class.getCanonicalName() + "_on_create_init_db_start_ws_service").start();
    }

    public void justExit() {
        onTerminate();
        // System.exit(0);
    }

    @Override
    public void onTerminate() {
        synchronized (STALE_NOTIFICATION_ID) {
            if (!isExiting) {
                isExiting = true;
                if (boundWebsocketService) {
                    unbindService(websocketServiceConnection);
                    stopService(websocketServiceIntent);
                }
                webSocketServiceConnectionEventHelper.clearListener();
            }
        }

        try {
            myShizukuContext.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        notificationManager.cancel(STALE_NOTIFICATION_ID);
        MyActivityManager.finishAllActivity();
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


    public void addWebSocketServiceConnectionListener(WebSocketServiceConnectionListener listener) {
        webSocketServiceConnectionEventHelper.addListener(listener);
    }

    public boolean removeWebSocketServiceConnectionListener(WebSocketServiceConnectionListener listener) {
        return webSocketServiceConnectionEventHelper.removeListener(listener);
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

    public PermissionManager getPermissionManager(Activity activity) {
        if (permissionManager == null) {
            permissionManager = new PermissionManager.Builder(activity)
                .withShizuku(myShizukuContext)
                .withPermissions(androidPermissionList)
                .build();
        } else {
            permissionManager.setActivity(activity);
        }

        return permissionManager;
    }
}
