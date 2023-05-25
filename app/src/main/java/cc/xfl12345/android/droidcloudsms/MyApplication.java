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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import org.teasoft.bee.android.CreateAndUpgradeRegistry;
import org.teasoft.beex.android.ApplicationRegistry;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import cc.xfl12345.android.droidcloudsms.model.BeeCreateAndUpgrade;
import cc.xfl12345.android.droidcloudsms.model.MyShizukuContext;
import cc.xfl12345.android.droidcloudsms.model.NotificationUtils;
import cc.xfl12345.android.droidcloudsms.model.PermissionItem;

public class MyApplication extends Application {
    public static final int STALE_NOTIFICATION_ID = 0;

    public static final String SP_KEY_APP_CONFIG = "AppConfig";

    public static final String SP_KEY_WEBSOCKET_SERVER_URL = "websocketServerURL";

    private Context context;

    private MyShizukuContext myShizukuContext;

    public MyShizukuContext getMyShizukuContext() {
        return myShizukuContext;
    }

    private NotificationManager notificationManager;

    public static List<Map.Entry<String, String>> androidPermissionList = new ArrayList<>();

    public static List<PermissionItem> permissionList;

    private Intent websocketServiceIntent;

    private ServiceConnection websocketServiceConnection;

    private boolean boundWebsocketService = false;

    private boolean connected2WebsocketService = false;

    public boolean isConnected2WebsocketService() {
        return connected2WebsocketService;
    }

    private WebsocketService.WebsocketServiceBinder binder;

    private WebsocketService websocketService = null;

    public WebsocketService getWebsocketService() {
        return websocketService;
    }

    private Boolean isExiting = false;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        myShizukuContext = new MyShizukuContext(context);

        androidPermissionList = new ArrayList<>(10);
        androidPermissionList.add(Map.entry("android.permission.INTERNET", "联网权限"));
        androidPermissionList.add(Map.entry("android.permission.VIBRATE", "震动权限"));
        androidPermissionList.add(Map.entry("android.permission.FOREGROUND_SERVICE", "前台服务权限"));
        androidPermissionList.add(Map.entry(Permission.NOTIFICATION_SERVICE, "通知栏权限"));
        androidPermissionList.add(Map.entry(Permission.POST_NOTIFICATIONS, "发送通知权限"));
        androidPermissionList.add(Map.entry(Permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "忽略电池优化权限"));
        initPermissionList();

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

        ApplicationRegistry.register(this);//注册上下文
        CreateAndUpgradeRegistry.register(BeeCreateAndUpgrade.class);

        // 吊起前台保活服务
        websocketServiceIntent = new Intent().setClass(getApplicationContext(), WebsocketService.class);
        websocketServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                connected2WebsocketService = true;
                binder = (WebsocketService.WebsocketServiceBinder) iBinder;
                websocketService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                connected2WebsocketService = false;
            }
        };
        boundWebsocketService = bindService(websocketServiceIntent, websocketServiceConnection, Context.BIND_AUTO_CREATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(websocketServiceIntent);
        } else {
            startService(websocketServiceIntent);
        }
    }

    public void justExit() {
        onTerminate();
        // System.exit(0);
    }

    @Override
    public void onTerminate() {
        synchronized (SP_KEY_APP_CONFIG) {
            if (!isExiting) {
                isExiting = true;
                if (boundWebsocketService) {
                    unbindService(websocketServiceConnection);
                    stopService(websocketServiceIntent);
                }
            }
        }

        // 注销通用通知回调
        NotificationUtils.unregisterReceiver(context);
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

    private void initPermissionList() {
        permissionList = new ArrayList<>(MyApplication.androidPermissionList.size() + 2);
        // 添加 Shizuku 权限选项
        permissionList.add(new PermissionItem() {
            @Override
            public String getDisplayName() {
                return "Shizuku";
            }

            @Override
            public String getCodeName() {
                return "Shizuku";
            }

            @Override
            public boolean isGranted() {
                return myShizukuContext.isGranted();
            }

            @Override
            public void requestPermission(boolean beforeRequestStatus, boolean targetStatus) {
                if (targetStatus) {
                    getRequestPermissionCallback().callback(
                        beforeRequestStatus,
                        myShizukuContext.requirePermission(),
                        targetStatus
                    );
                } else {
                    // 暂时还不支持撤销授权，这种操作
                    getRequestPermissionCallback().callback(
                        beforeRequestStatus,
                        isGranted(),
                        targetStatus
                    );
                }
            }
        });
        // 添加 安卓权限选项
        androidPermissionList.forEach(entry -> permissionList.add(new PermissionItem() {
            @Override
            public String getDisplayName() {
                return entry.getValue();
            }

            @Override
            public String getCodeName() {
                return entry.getKey();
            }

            @Override
            public void requestPermission(boolean beforeRequestStatus, boolean targetStatus) {
                if (targetStatus) {
                    XXPermissions.with(context)
                        .permission(getCodeName())
                        .request(new OnPermissionCallback() {
                            @Override
                            public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                getRequestPermissionCallback().callback(
                                    beforeRequestStatus,
                                    true,
                                    targetStatus
                                );
                            }

                            @Override
                            public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                getRequestPermissionCallback().callback(
                                    beforeRequestStatus,
                                    false,
                                    targetStatus
                                );
                            }
                        });
                } else {
                    // 暂时还不支持撤销授权，这种操作
                    getRequestPermissionCallback().callback(
                        beforeRequestStatus,
                        isGranted(),
                        targetStatus
                    );
                }
            }

            @Override
            public boolean isGranted() {
                return XXPermissions.isGranted(context, getCodeName());
            }
        }));
    }

    public boolean isAllPermissionGranted() {
        boolean isAllGranted = true;
        // 遍历检查全部权限情况
        for (PermissionItem item : permissionList) {
            if (!item.isGranted()) {
                isAllGranted = false;
                break;
            }
        }

        return isAllGranted;
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
