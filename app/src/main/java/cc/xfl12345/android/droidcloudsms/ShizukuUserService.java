package cc.xfl12345.android.droidcloudsms;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import android.system.Os;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.gotev.cookiestore.SharedPreferencesCookieStore;
import net.gotev.cookiestore.WebKitSyncCookieManager;
import net.gotev.cookiestore.okhttp.JavaNetCookieJar;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import cc.xfl12345.android.droidcloudsms.model.BaseEventAmplifier;
import cc.xfl12345.android.droidcloudsms.model.HiddenApiBypassProxy;
import cc.xfl12345.android.droidcloudsms.model.IdGenerator;
import cc.xfl12345.android.droidcloudsms.model.SmSender;
import cc.xfl12345.android.droidcloudsms.model.http.response.JsonApiResponseData;
import cc.xfl12345.android.droidcloudsms.model.ws.SmsTaskRequestObject;
import cc.xfl12345.android.droidcloudsms.model.ws.WebSocketManager;
import cc.xfl12345.android.droidcloudsms.model.ws.WebSocketMessage;
import inet.ipaddr.HostName;
import okhttp3.CookieJar;
import okhttp3.Dns;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.dnsoverhttps.DnsOverHttps;
import rikka.shizuku.Shizuku;
import rikka.shizuku.SystemServiceHelper;

// source code URL=https://blog.csdn.net/yxl930401/article/details/127963284
// source code URL=https://blog.csdn.net/weixin_35691921/article/details/124419935
public class ShizukuUserService extends IShizukuUserService.Stub {

    private String TAG = ShizukuUserService.class.getName();

    private Context context = null;

    public ShizukuUserService() {
        // binder = new ServiceBinder();
        Log.d(TAG, "无参构造已触发！");
    }

    public ShizukuUserService(Context context) {
        this();
        this.context = context;
        Log.d(TAG, "有参构造已触发！context.getPackageName()=" + context.getPackageName());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void justTest() throws RemoteException {
        try {
            synchronized (Looper.class) {
                // if (Looper.getMainLooper() == null)
                    Looper.prepare();
            }
            Log.d(TAG, "context.getPackageName()=" + context.getPackageName());
            Os.setuid(1000);
            Log.d(TAG, "Os.getuid()=" + Os.getuid());
            // Class<?> activityThreadClass = activityThreadClass = Class.forName("android.app.ActivityThread");
            // Method systemMain = HiddenApiBypassProxy.getDeclaredMethod(activityThreadClass, "systemMain");
            // Method getSystemContext = HiddenApiBypassProxy.getDeclaredMethod(activityThreadClass, "getSystemContext");
            // Object systemMainThread = systemMain.invoke(null);
            // Context systemContext = (Context) getSystemContext.invoke(systemMainThread);
            Class<?> activityThreadClass = activityThreadClass = Class.forName("android.app.ActivityThread");
            Method systemMain = HiddenApiBypassProxy.getDeclaredMethod(activityThreadClass, "systemMain");
            Method getSystemContext = HiddenApiBypassProxy.getDeclaredMethod(activityThreadClass, "getSystemContext");
            Context systemContext = (Context) getSystemContext.invoke(systemMain.invoke(null));
            TelephonyManager telephonyManager = HiddenApiBypassProxy.getDeclaredConstructor(TelephonyManager.class, Context.class).newInstance(systemContext);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "telephonyManager.getImei()=" + telephonyManager.getImei());
            }
            Log.d(TAG, "telephonyManager.getAllCellInfo()=" + telephonyManager.getAllCellInfo());
            // Constructor<TelephonyManager> constructor = HiddenApiBypassProxy.getDeclaredConstructor(TelephonyManager.class, Context.class);
            // Constructor<TelephonyManager> constructor = TelephonyManager.class.getDeclaredConstructor(Context.class);
            // constructor.setAccessible(true);
            // TelephonyManager telephonyManager = constructor.newInstance(context);
            // TelephonyManager telephonyManager = context.getSystemService(TelephonyManager.class);



            // TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //     Log.d(TAG, "telephonyManager.getImei()=" + telephonyManager.getImei());
            // }
            // Log.d(TAG, "telephonyManager.getAllCellInfo()=" + telephonyManager.getAllCellInfo());

            // ActivityManager activityManager = context.getSystemService(ActivityManager.class);

            // Method createPackageContext = HiddenApiBypassProxy.getDeclaredMethod(Context.class, "createPackageContext", String.class, int.class);
            // Context systemContext = (Context) createPackageContext.invoke(context, "android", Context.CONTEXT_INCLUDE_CODE);
            // Log.d(TAG, "systemContext.getPackageName()=" + systemContext.getPackageName());

            // SmsManager smsManager = SmsManager.getDefault();
            // Log.d(TAG, "smsManager.getSubscriptionId()=" + smsManager.getSubscriptionId());

            Log.d(TAG, "My UID=" + Os.getuid());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() == null ? e.toString() : e.getMessage(), e);
        }
    }

    @Override
    public void destroy() throws RemoteException {
        Log.i(TAG, "退出指令已收到。");
        System.exit(0);
    }
}
