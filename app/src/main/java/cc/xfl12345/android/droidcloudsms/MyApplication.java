package cc.xfl12345.android.droidcloudsms;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hjq.permissions.Permission;
import com.topjohnwu.superuser.Shell;

import org.teasoft.bee.android.CreateAndUpgradeRegistry;
import org.teasoft.beex.android.ApplicationRegistry;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cc.xfl12345.android.droidcloudsms.model.AndroidPermissionNamePair;
import cc.xfl12345.android.droidcloudsms.model.BeeCreateAndUpgrade;
import cc.xfl12345.android.droidcloudsms.model.HiddenApiBypassProxy;
import cc.xfl12345.android.droidcloudsms.model.MyShizukuContext;
import cc.xfl12345.android.droidcloudsms.model.NotificationUtils;
import cc.xfl12345.android.droidcloudsms.model.PermissionManager;
import cc.xfl12345.android.droidcloudsms.model.SystemServiceBinderHelper;
import cc.xfl12345.android.droidcloudsms.model.WebSocketServiceConnectionEventHelper;
import cc.xfl12345.android.droidcloudsms.model.WebSocketServiceConnectionListener;
import rikka.shizuku.Shizuku;
import rikka.sui.Sui;

public class MyApplication extends Application {

    @SuppressLint("MissingPermission")
    private void hackSystem() {
        try {
            System.out.println("Current UID=" + Shizuku.getUid());
            // Set settings before the main shell can be created
            Shell.enableVerboseLogging = true;
            Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
            );


            System.out.println("Current UID=" + Shizuku.getUid());
            Sui.init(getPackageName());
            System.out.println("Current UID=" + Shizuku.getUid());

            // Shell.Result result = Shell.cmd("find /dev/block -iname boot").exec();
            Pattern pattern = Pattern.compile("\t.+:");
            List<String> cmdResult = Shell.cmd("service list").exec().getOut();
            List<String> serviceNames = cmdResult
                .stream()
                .map(item -> {
                    Matcher matcher = pattern.matcher(item);
                    if (matcher.find()) {
                        String tmp = matcher.group();
                        return tmp.substring(1, tmp.length() - 1);
                    }
                    return "";
                })
                .filter(item -> !"".equals(item))
                .collect(Collectors.toList());
            System.out.println(serviceNames);

            Set<Class<?>> normalJavaDataType = new HashSet<>(60);
            normalJavaDataType.add(int.class);
            normalJavaDataType.add(long.class);
            normalJavaDataType.add(short.class);
            normalJavaDataType.add(float.class);
            normalJavaDataType.add(double.class);
            normalJavaDataType.add(char.class);
            normalJavaDataType.add(byte.class);
            normalJavaDataType.add(boolean.class);
            normalJavaDataType.add(void.class);
            normalJavaDataType.add(int[].class);
            normalJavaDataType.add(long[].class);
            normalJavaDataType.add(short[].class);
            normalJavaDataType.add(float[].class);
            normalJavaDataType.add(double[].class);
            normalJavaDataType.add(char[].class);
            normalJavaDataType.add(byte[].class);
            normalJavaDataType.add(boolean[].class);
            normalJavaDataType.add(CharSequence.class);
            normalJavaDataType.add(String.class);
            normalJavaDataType.add(Integer.class);
            normalJavaDataType.add(Long.class);
            normalJavaDataType.add(Short.class);
            normalJavaDataType.add(Float.class);
            normalJavaDataType.add(Double.class);
            normalJavaDataType.add(Character.class);
            normalJavaDataType.add(Byte.class);
            normalJavaDataType.add(Boolean.class);
            normalJavaDataType.add(Void.class);
            normalJavaDataType.add(CharSequence[].class);
            normalJavaDataType.add(String[].class);
            normalJavaDataType.add(Integer[].class);
            normalJavaDataType.add(Long[].class);
            normalJavaDataType.add(Short[].class);
            normalJavaDataType.add(Float[].class);
            normalJavaDataType.add(Double[].class);
            normalJavaDataType.add(Character[].class);
            normalJavaDataType.add(Byte[].class);
            normalJavaDataType.add(Boolean[].class);
            normalJavaDataType.add(Intent.class);

            List<Method> methodList = new LinkedList<>();
            List<String> methodNameList = new LinkedList<>();
            for (String serviceName : serviceNames) {
                try {
                    SystemServiceBinderHelper helper = new SystemServiceBinderHelper(serviceName);
                    helper.getServiceClassDeclaredMethods().forEach(item -> {
                        if (!normalJavaDataType.contains(item.getReturnType())) {
                            methodList.add(item);
                            methodNameList.add(item.toString());
                            if (Context.class.isAssignableFrom(item.getReturnType()) || item.getReturnType().isAssignableFrom(Context.class)) {
                                System.out.println("Found !!!! ----> " + item.toString());
                            }
                        }
                    });

                } catch (Exception e) {
                    // ignore
                }
            }


            // XposedHelpers.findField()

            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method systemMain = HiddenApiBypassProxy.getDeclaredMethod(activityThreadClass, "systemMain");
            Method getSystemContext = HiddenApiBypassProxy.getDeclaredMethod(activityThreadClass, "getSystemContext");
            Context systemContext = (Context) getSystemContext.invoke(systemMain.invoke(null));
            TelephonyManager telephonyManager = HiddenApiBypassProxy.getDeclaredConstructor(TelephonyManager.class, Context.class).newInstance(systemContext);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                System.out.println(telephonyManager.getImei());
            }
            System.out.println(telephonyManager.getAllCellInfo());


            System.out.println(methodNameList);

        } catch (Exception e) {
            System.err.println(e);
        }

        System.exit(0);
    }

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
    Shizuku.UserServiceArgs userServiceArgs;
    ServiceConnection shizukuUserServiceConnection;

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
        // hackSystem();

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

        // 初始化数据库框架
        ApplicationRegistry.register(this);// 注册上下文
        CreateAndUpgradeRegistry.register(BeeCreateAndUpgrade.class);


        Sui.init("cc.xfl12345.android.droidcloudsms");
        myShizukuContext.requirePermission();
        // Context context2 = getApplicationContext();
        // try {
        //     Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        //     Method systemMain = HiddenApiBypassProxy.getDeclaredMethod(activityThreadClass, "systemMain");
        //     Method getSystemContext = HiddenApiBypassProxy.getDeclaredMethod(activityThreadClass, "getSystemContext");
        //     context2 = (Context) getSystemContext.invoke(systemMain.invoke(null));
        // } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
        //     // ignore
        // }
       userServiceArgs = new Shizuku.UserServiceArgs(new ComponentName(getApplicationContext(), ShizukuUserService.class))
            .debuggable(true)
            .daemon(false)
            .processNameSuffix("sui")
            // .tag(getClass().getName())
            .version(5);

       shizukuUserServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                try {
                    Log.d("啦啦啦Shizuku", "有回调了！");
                    NotificationUtils.postNotification(context, "Shizuku", "有回调了！");
                    IShizukuUserService.Stub.asInterface(service).justTest();
                } catch (RemoteException e) {
                    Log.e("啦啦啦Shizuku", e.getMessage() == null ? e.toString() : e.getMessage());
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }

            @Override
            public void onBindingDied(ComponentName name) {
                Log.e("啦啦啦Shizuku", "Sui binding died");
            }

            @Override
            public void onNullBinding(ComponentName name) {
                Log.e("啦啦啦Shizuku", "Sui binding is null");
            }
        };
        // Shizuku.unbindUserService(userServiceArgs, shizukuUserServiceConnection, true);
        if (Shizuku.peekUserService(userServiceArgs, shizukuUserServiceConnection) < 0) {
            Shizuku.bindUserService(userServiceArgs, shizukuUserServiceConnection);
        }


        // startService(shizukuUserServiceIntent);
        // Intent shizukuUserServiceIntent = new Intent().setClass(getApplicationContext(), ShizukuUserService.class);
        // bindService(shizukuUserServiceIntent, new ServiceConnection() {
        //     @Override
        //     public void onServiceConnected(ComponentName name, IBinder service) {
        //
        //     }
        //
        //     @Override
        //     public void onServiceDisconnected(ComponentName name) {
        //
        //     }
        // }, BIND_AUTO_CREATE);


        // 吊起前台保活服务
        websocketServiceIntent = new Intent().setClass(getApplicationContext(), WebsocketService.class);
        websocketServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                webSocketServiceConnectionEventHelper.onServiceConnected(((WebsocketService.ServiceBinder) iBinder).getService());
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

        try {
            Shizuku.unbindUserService(userServiceArgs, shizukuUserServiceConnection, true);
        } catch (Exception e) {
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
