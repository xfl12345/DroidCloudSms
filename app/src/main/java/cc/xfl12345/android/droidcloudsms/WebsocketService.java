package cc.xfl12345.android.droidcloudsms;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import cc.xfl12345.android.droidcloudsms.model.IdGenerator;
import cc.xfl12345.android.droidcloudsms.model.SmSender;
import cc.xfl12345.android.droidcloudsms.model.http.response.JsonApiResponseData;
import cc.xfl12345.android.droidcloudsms.model.ws.SmsTaskRequestObject;
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
import okio.ByteString;
import rikka.shizuku.Shizuku;

// source code URL=https://blog.csdn.net/yxl930401/article/details/127963284
// source code URL=https://blog.csdn.net/weixin_35691921/article/details/124419935
public class WebsocketService extends Service implements
    Shizuku.OnRequestPermissionResultListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String CLEAR_NOTIFICATION_ACTION = "cc.xfl12345.android.droidcloudsms.WebsocketService.CLEAR_NOTIFICATION_ACTION";

    private static final String TAG = "DroidCloudSmsWebSocketService";

    public static final String SERVICE_NAME = TAG;

    public static final String CHANNEL_ID = TAG;

    public static final String SERVICE_NOTIFICATION_CHANNEL_NAME = "SmsWebsocket";

    public static final int SERVICE_ID_INT = 1;

    private final IBinder binder = new WebsocketServiceBinder();

    private MyApplication context;

    private NotificationManager notificationManager;

    private SmSender smSender = null;

    public SmSender getSmSender() {
        return smSender;
    }

    public boolean isSmsReady() {
        return smSender != null;
    }

    private boolean isClosing = false;

    private WebSocket ws;

    private boolean websocketInitialized = false;

    public boolean isWebsocketInitialized() {
        return websocketInitialized;
    }

    private boolean websocketConnected = false;

    public boolean isWebsocketConnected() {
        return websocketConnected;
    }

    private BroadcastReceiver broadcastReceiver;

    private NotificationManager getNotificationManager() {
        return ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE));
    }

    private final IdGenerator notificationIdGenerator = new IdGenerator(1);

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private String getSharedPreferencesName() {
        String name = context.getPackageName() + "_preferences";
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, MODE_PRIVATE);
        if (sharedPreferences.equals(getSharedPreferences())) {
            return name;
        } else {
            try {
                Method method = PreferenceManager.class.getDeclaredMethod("getDefaultSharedPreferencesName", Context.class);
                method.setAccessible(true);
                String tmp = (String) method.invoke(null, context);
                if (tmp != null) {
                    return tmp;
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                // ignore
            }
        }

        postNotification("获取默认 SharedPreferences 的名字失败！");
        return name;
    }

    private Dns[] okHttpDnsArray = new Dns[0];

    private OkHttpClient okHttpBootstrapClient = null;

    private CookieManager cookieManager;

    public WebsocketService() {
    }

    // 用于Activity和service通讯
    public class WebsocketServiceBinder extends Binder {
        public WebsocketService getService() {
            return WebsocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isClosing = false;
        context = (MyApplication) getApplicationContext();
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        initNotification();
        initSmSender();
        Shizuku.addRequestPermissionResultListener(this);


        cookieManager = new WebKitSyncCookieManager(
            new SharedPreferencesCookieStore(this, getSharedPreferencesName()),
            CookiePolicy.ACCEPT_ALL,
            exception -> {
                postNotification("WebKitSyncCookieManager 创建失败！调试信息：" + exception.getMessage());
                return null;
            }
        );

        CookieManager.setDefault(cookieManager);
        okHttpBootstrapClient = new OkHttpClient.Builder().build();
        if (isInChina()) {
            okHttpDnsArray = new Dns[]{
                // // 腾讯云 DNSPod DNS DoH
                generateOkHttpDns("https://120.53.53.53/dns-query"),
                // // 阿里云 AliDNS DoH
                generateOkHttpDns("https://223.5.5.5/dns-query"),
                // // Cloudflare 备用DNS IPv6 DoH
                generateOkHttpDns("https://[2606:4700:4700::1001]/dns-query"),
                generateOkHttpDns("https://120.53.53.53/dns-query"),
                Dns.SYSTEM
            };
        } else {
            okHttpDnsArray = new Dns[]{
                // Cloudflare 主力DNS IPv4 DoH
                generateOkHttpDns("https://1.1.1.1/dns-query"),
                // Cloudflare 主力DNS IPv6 DoH
                generateOkHttpDns("https://[2606:4700:4700::1111]/dns-query"),
                // Google DNS DoH
                generateOkHttpDns("https://dns.google/dns-query"),
                Dns.SYSTEM,
                // OpenDNS 主力DNS IPv4 DoH
                generateOkHttpDns("https://208.67.222.222/dns-query")
            };
        }

        reinitWebSocket();
        Log.i(TAG, "onCreate...");
    }

    private void initNotification() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // 8.0以下
            startForeground(SERVICE_ID_INT, new Notification());
        } else {
            // 8.0以及以上
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                // NotificationManager.IMPORTANCE_MIN 通知栏消息的重要级别  最低，不让弹出
                // IMPORTANCE_MIN 前台时，在阴影区能看到，后台时 阴影区不消失，增加显示 IMPORTANCE_NONE时 一样的提示
                // IMPORTANCE_NONE app在前台没有通知显示，后台时有
                NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
                if (channel == null) {
                    channel = new NotificationChannel(
                        CHANNEL_ID,
                        SERVICE_NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    );
                    // 启用震动
                    channel.enableVibration(true);
                    // 渐进式震动（先震动 0.8 秒，然后停止 0.5 秒，再震动 1.2 秒）
                    channel.setVibrationPattern(new long[]{800, 500, 1200});
                    notificationManager.createNotificationChannel(channel);
                }
                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setOngoing(true).build();
                startForeground(SERVICE_ID_INT, notification);
            }
        }

        IntentFilter filter = new IntentFilter(CLEAR_NOTIFICATION_ACTION);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int sequence = intent.getIntExtra("sequence", -1);
            }
        };

        ContextCompat.registerReceiver(context, broadcastReceiver, filter, ContextCompat.RECEIVER_EXPORTED);
    }

    private Dns getCachedOkHttpDns(String hostName, List<InetAddress> records) {
        return (Dns) domain -> {
            // 使用已有解析
            if (domain.equals(hostName) && records.size() > 0) {
                return records;
            }

            return Dns.SYSTEM.lookup(domain);
        };
    }

    public Thread reinitWebSocket() {
        Thread thread = new Thread(() -> {
            synchronized (TAG) {
                if (websocketInitialized) {
                    closeWebSocket();
                }
                if (isClosing) {
                    return;
                }
            }

            SharedPreferences sharedPreferences = getSharedPreferences();
            String loginUrlInText = sharedPreferences.getString(MyApplication.SP_KEY_WEBSOCKET_SERVER_LOGIN_URL, "");
            String accessKeySecret = sharedPreferences.getString(MyApplication.SP_KEY_WEBSOCKET_SERVER_ACCESS_KEY_SECRET, "");
            String connectUrlInText = "";
            String ipAddress = "";
            String hostName = "";
            List<InetAddress> records = Collections.emptyList();
            URL loginURL = null;
            URI loginURI = null;
            CookieJar cookieJar = new JavaNetCookieJar(cookieManager);
            Dns dns = Dns.SYSTEM;

            // 尝试登录
            if (!"".equals(loginUrlInText)) {
                try {
                    HttpUrl okhttpLoginUrl = HttpUrl.get(loginUrlInText);
                    loginURL = new URL(loginUrlInText);
                    // loginURI = loginURL.toURI();

                    // 先开展解析工作
                    hostName = loginURL.getHost();
                    HostName tmpHostName = new HostName(hostName);
                    if (tmpHostName.isAddress()) {
                        ipAddress = tmpHostName.getHost();
                    } else {
                        // 遍历 5 遍 DNS 列表，反复查询 DNS ，应付极端缓慢的海外域名解析
                        boolean isOk = false;
                        for (int i = 0; !isOk && i < 5; i++) {
                            try {
                                for (Dns item : okHttpDnsArray) {
                                    records = item.lookup(tmpHostName.getHost());

                                    if (records.size() > 0) {
                                        ipAddress = records.get(0).getHostAddress();
                                        isOk = true;
                                        break;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (records.size() == 0) {
                            postNotification("WebSocket登录URL域名解析失败！");
                        } else {
                            dns = getCachedOkHttpDns(hostName, records);
                        }
                    }

                    // 尝试登录、拿到 Cookie
                    if (ipAddress != null && !"".equals(ipAddress)) {
                        OkHttpClient client = new OkHttpClient.Builder()
                            .cookieJar(cookieJar)
                            .dns(dns)
                            .build();
                        FormBody formBody = new FormBody.Builder()
                            .add("accessKeySecret", accessKeySecret)
                            .build();

                        Request request = new Request.Builder()
                            .url(okhttpLoginUrl)
                            .post(formBody)
                            .build();

                        try {
                            Response loginResponse = client.newCall(request).execute();
                            // 服务端返回的结果
                            if (loginResponse.isSuccessful()) {
                                try {
                                    String loginResponsePayload = loginResponse.body().string();
                                    JsonApiResponseData responseData = new Gson().fromJson(loginResponsePayload, JsonApiResponseData.class);
                                    if (responseData.isSuccess()) {
                                        URL connectURL = new URL(loginURL, "./ws-connect");
                                        String tmpConnectURLInText = connectURL.toString();
                                        if (tmpConnectURLInText.startsWith("http")) {
                                            connectUrlInText = "ws" + tmpConnectURLInText.substring(4);
                                        } else {
                                            connectUrlInText = tmpConnectURLInText;
                                        }
                                    } else {
                                        postNotification("WebSocket登录失败！调试消息：" + loginResponsePayload);
                                    }
                                } catch (JsonSyntaxException e) {
                                    postNotification("WebSocket登录请求回执内容解析失败！调试消息：" + e.getMessage());
                                }
                            } else {
                                postNotification("WebSocket登录请求失败！调试消息：" + loginResponse.body());
                            }
                            loginResponse.close();
                        } catch (IOException e) {
                            postNotification("WebSocket连接失败！调试消息：" + e.getMessage());
                        }
                    }
                } catch (MalformedURLException e) {
                    postNotification("WebSocket登录URL格式错误！调试消息：" + e.getMessage());
                }
            }

            // 走到这一步，前面流程务必登录成功
            // 尝试连接 WebSocket
            if (!"".equals(connectUrlInText)) {
                OkHttpClient websocketOkHttpClient = new OkHttpClient.Builder()
                    .callTimeout(3, TimeUnit.MINUTES)
                    .pingInterval(40, TimeUnit.SECONDS)
                    .cookieJar(cookieJar)
                    .dns(dns)
                    .build();

                Request request = new Request.Builder()
                    .url(connectUrlInText)
                    .build();

                WebSocketListener webSocketListener = new WebSocketListener() {
                    @Override
                    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                        websocketConnected = false;
                        super.onClosed(webSocket, code, reason);
                    }

                    @Override
                    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                        super.onClosing(webSocket, code, reason);
                    }

                    @Override
                    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                        super.onFailure(webSocket, t, response);
                    }

                    @Override
                    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                        try {
                            WebSocketMessage message = new Gson().fromJson(text, WebSocketMessage.class);
                            if (WebSocketMessage.Type.request.equals(message.getMessageType())) {
                                JsonObject jsonObject = new Gson().toJsonTree(message.getPayload()).getAsJsonObject();
                                if ("sendSms".equals(jsonObject.get("operation").getAsString())) {
                                    SmsTaskRequestObject requestObject = new Gson().fromJson(jsonObject, SmsTaskRequestObject.class);
                                    smSender.sendMessage(requestObject.data.getPhoneNumber(), requestObject.data.getSmsContent());
                                }
                            }
                        } catch (Exception e) {
                            postNotification("收到消息，但解析发生错误。调试消息：" + e.getMessage());
                        }
                    }

                    @Override
                    public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                        super.onMessage(webSocket, bytes);
                    }

                    @Override
                    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                        websocketConnected = true;
                        super.onOpen(webSocket, response);
                    }
                };

                try {
                    ws = websocketOkHttpClient.newWebSocket(request, webSocketListener);
                    websocketInitialized = true;
                    postNotification("WebSocket 连接成功！");
                } catch (Exception e) {
                    postNotification("WebSocket 连接失败！调试消息：" + e.getMessage());
                }

            }
        });
        thread.start();

        return thread;
    }

    public void closeWebSocket() {
        if (ws != null && websocketInitialized) {
            boolean shutDownFlag = ws.close(1000, "manual close");
            postNotification("WebSocket 关闭" + (shutDownFlag ? "成功" : "失败"));
            ws.cancel();
            ws = null;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy...");
        synchronized (TAG) {
            isClosing = true;
            if (websocketInitialized) {
                closeWebSocket();
            }
        }

        context.unregisterReceiver(broadcastReceiver);
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        Shizuku.removeRequestPermissionResultListener(this);

        if (smSender != null) {
            try {
                smSender.close();
            } catch (IOException e) {
                // ignore
            }
        }

        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        stopSelf();
        System.exit(0);
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind...");
        return binder;
    }


    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.i(TAG, "onRebind...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private void initSmSender() {
        if (context.getMyShizukuContext().refreshPermissionStatus()) {
            try {
                if (smSender != null) {
                    smSender.close();
                }
                smSender = new SmSender(context);
                postNotification("创建短信服务成功！");
            } catch (ReflectiveOperationException | RemoteException | IOException e) {
                postNotification("创建短信服务失败！原因：" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            postNotification("创建短信服务失败！原因：" + "Shizuku 未授权");
        }
    }

    private int postNotification(String content) {
        int requestCode = notificationIdGenerator.generate();

        // 设置取消后的动作
        Intent intent = new Intent(CLEAR_NOTIFICATION_ACTION);
        intent.putExtra("sequence", requestCode);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        // 初始化 notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("核心服务")    // 设置标题
            .setContentText(content)    // 设置通知文字
            .setSmallIcon(R.drawable.baseline_contact_mail_24)   // 设置左边的小图标
            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.miyamizu_mitsuha_head))  // 设置大图标
            // .setColor(Color.parseColor("#ff0000"))
            .setContentIntent(pendingIntent)  // 设置点击通知之后 进入相关页面(此处进入NotificationActivity类，执行oncreat方法打印日志)
            .setOngoing(false)
            .setAutoCancel(true)   // 设置点击通知后 通知通知栏不显示 （但实测不行，目前使用 pendingIntent 回调来删除通知）
            .build();

        getNotificationManager().notify(CHANNEL_ID, requestCode, notification);

        return requestCode;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            initSmSender();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (MyApplication.SP_KEY_WEBSOCKET_SERVER_LOGIN_URL.equals(key)) {
            String secret = sharedPreferences.getString(MyApplication.SP_KEY_WEBSOCKET_SERVER_ACCESS_KEY_SECRET, null);
            if (secret != null) {
                reinitWebSocket();
            }
        }
        if (MyApplication.SP_KEY_WEBSOCKET_SERVER_ACCESS_KEY_SECRET.equals(key)) {
            String url = sharedPreferences.getString(MyApplication.SP_KEY_WEBSOCKET_SERVER_LOGIN_URL, null);
            if (url != null) {
                reinitWebSocket();
            }
        }
    }

    // 简单通过时区判断是否在大陆
    public boolean isInChina() {
        try {
            TimeZone zone = TimeZone.getDefault();
            String id = zone.getID();
            boolean result;
            switch (id) {
                case "Asia/Shanghai":
                case "Asia/Chongqing":
                case "Asia/Harbin":
                case "Asia/Urumqi":
                    result = true;
                    break;
                default:
                    result = false;
                    break;
            }

            return result;
        } catch (Exception e) {
            return false;
        }
    }

    private DnsOverHttps generateOkHttpDns(String urlInText) {
        return new DnsOverHttps.Builder().client(okHttpBootstrapClient).url(HttpUrl.get(urlInText)).build();
    }

}
