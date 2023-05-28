package cc.xfl12345.android.droidcloudsms;

import android.annotation.SuppressLint;
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
import android.net.InetAddresses;
import android.net.SSLCertificateSocketFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.neovisionaries.ws.client.DualStackMode;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.qiniu.android.dns.DnsManager;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;
import com.qiniu.android.dns.dns.DnsUdpResolver;
import com.qiniu.android.dns.dns.DohResolver;
import com.qiniu.android.dns.local.AndroidDnsServer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.SSLContext;

import cc.xfl12345.android.droidcloudsms.model.IdGenerator;
import cc.xfl12345.android.droidcloudsms.model.NotificationUtils;
import cc.xfl12345.android.droidcloudsms.model.SmSender;
import cc.xfl12345.android.droidcloudsms.model.http.response.JsonApiResponseData;
import cc.xfl12345.android.droidcloudsms.model.ws.SmsTaskRequestObject;
import cc.xfl12345.android.droidcloudsms.model.ws.WebSocketMessage;
import inet.ipaddr.HostName;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rikka.shizuku.Shizuku;

// source code URL=https://blog.csdn.net/yxl930401/article/details/127963284
// source code URL=https://blog.csdn.net/weixin_35691921/article/details/124419935
public class WebsocketService extends Service implements
    Shizuku.OnRequestPermissionResultListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String CLEAR_COMMON_NOTIFICATION_ACTION = "cc.xfl12345.android.droidcloudsms.WebsocketService.CLEAR_COMMON_NOTIFICATION_ACTION";

    private static final String TAG = "DroidCloudSmsWebSocketService";

    public static final String SERVICE_NAME = TAG;

    public static final String CHANNEL_ID = TAG;

    public static final String SERVICE_NOTIFICATION_CHANNEL_NAME = "SmsWebsocket";

    public static final int SERVICE_ID_INT = 1;

    private final IBinder binder = new WebsocketServiceBinder();

    private MyApplication context;

    private SmSender smSender = null;

    public SmSender getSmSender() {
        return smSender;
    }

    public boolean isSmsReady() {
        return getSmSender() != null;
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

    private DnsManager dns;

    public WebsocketService() {
    }

    // 用于Activity和service通讯
    public class WebsocketServiceBinder extends Binder {
        public WebsocketService getService() {
            return WebsocketService.this;
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate() {
        super.onCreate();
        isClosing = false;
        context = (MyApplication) getApplicationContext();
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // 8.0以下
            startForeground(SERVICE_ID_INT, new Notification());
        } else {
            // 8.0以及以上
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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

        IntentFilter filter = new IntentFilter(CLEAR_COMMON_NOTIFICATION_ACTION);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int sequence = intent.getIntExtra("sequence", -1);
                if (sequence != -1) {
                    getNotificationManager().cancel(CHANNEL_ID, sequence);
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            context.registerReceiver(broadcastReceiver, filter);
        }


        if (DnsManager.needHttpDns()) {
            IResolver[] resolvers = new IResolver[] {
                AndroidDnsServer.defaultResolver(context),
                // // 腾讯云 DNSPod DNS DoH
                new DohResolver("https://120.53.53.53/dns-query", Record.TYPE_A, 2),
                new DohResolver("https://120.53.53.53/dns-query", Record.TYPE_AAAA, 2),
                // // 阿里云 AliDNS DoH
                new DohResolver("https://223.5.5.5/dns-query", Record.TYPE_A, 2),
                new DohResolver("https://223.5.5.5/dns-query", Record.TYPE_AAAA, 2),
                // // Cloudflare 备用DNS IPv6 DoH
                new DohResolver("https://[2606:4700:4700::1001]/dns-query", Record.TYPE_A, 2),
                new DohResolver("https://[2606:4700:4700::1001]/dns-query", Record.TYPE_AAAA, 2),
                // // 腾讯云 DNSPod DNS
                new DnsUdpResolver("119.29.29.29", Record.TYPE_A, 2),
                new DnsUdpResolver("119.29.29.29", Record.TYPE_AAAA, 2),
                // // 阿里云 AliDNS
                new DnsUdpResolver("223.5.5.5", Record.TYPE_A, 2),
                new DnsUdpResolver("223.5.5.5", Record.TYPE_AAAA, 2)

            };
            dns = new DnsManager(NetworkInfo.normal, resolvers);
        } else {
            IResolver[] resolvers = new IResolver[] {
                AndroidDnsServer.defaultResolver(context),
                // Cloudflare 主力DNS IPv4 DoH
                new DohResolver("https://1.1.1.1/dns-query", Record.TYPE_A, 2),
                new DohResolver("https://1.1.1.1/dns-query", Record.TYPE_AAAA, 2),
                // Cloudflare 主力DNS IPv6 DoH
                new DohResolver("https://[2606:4700:4700::1111]/dns-query"),
                // OpenDNS 主力DNS IPv4 DoH
                new DohResolver("https://208.67.222.222/dns-query"),
                // Google DNS DoH
                new DohResolver("https://dns.google/dns-query"),
            };
            dns = new DnsManager(NetworkInfo.normal, resolvers);
        }

        Shizuku.addRequestPermissionResultListener(this);
        initSmSender();
        reinitWsGo();

        Log.i(TAG, "onCreate...");
    }


    public void reinitWsGo() {
        new Thread(() -> {
            synchronized (TAG) {
                if (websocketInitialized) {
                    ws.sendClose();
                    websocketInitialized = false;
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

            final Map<Integer, List<Cookie>> cookies = new HashMap<>(Map.of(Integer.MAX_VALUE, Collections.emptyList()));

            // 尝试登录
            if (!"".equals(loginUrlInText)) {
                try {

                    HttpUrl okhttpLoginUrl = HttpUrl.parse(loginUrlInText);
                    URL loginURL = new URL(loginUrlInText);
                    boolean isOk2ConnectURL = true;

                    HostName hostName = new HostName(loginURL.getHost());
                    if (!hostName.isAddress()) {
                        // 遍历 5 遍 DNS 列表，反复查询 DNS ，应付极端缓慢的海外域名解析
                        Record[] records = new Record[0];
                        for (int i = 0; i < 5; i++) {
                            try {
                                records = dns.queryRecords(hostName.getHost());
                                if (records.length > 0) {
                                    Record happyDnsRecord = records[0];
                                    if (happyDnsRecord.isAAAA()) {
                                        String[] tmpArr = happyDnsRecord.value.split(":");
                                        StringBuilder stringBuilder = new StringBuilder(happyDnsRecord.value.length());
                                        for (String ipAddresFragment : tmpArr) {
                                            if (!"".equals(ipAddresFragment)) {
                                                stringBuilder.append(Integer.toHexString(Integer.parseInt(ipAddresFragment))).append(":");
                                            }
                                        }
                                        ipAddress = stringBuilder.substring(0, stringBuilder.length() - 2);
                                    } else {
                                        ipAddress = happyDnsRecord.value;
                                    }
                                    break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            try {
                                // 暂停 500 毫秒再重新询问
                                Thread.sleep(500);
                            } catch (InterruptedException ex) {
                                // ignore
                            }
                        }
                        if (records.length == 0) {
                            normalNotification("WebSocket登录URL域名解析失败！");
                            isOk2ConnectURL = false;
                        }
                    } else {
                        ipAddress = hostName.getHost();
                    }


                    if (isOk2ConnectURL) {
                        OkHttpClient client = new OkHttpClient.Builder()
                            .cookieJar(new CookieJar() {
                                @Override
                                public void saveFromResponse(@NonNull HttpUrl httpUrl, @NonNull List<Cookie> list) {
                                    cookies.put(Integer.MAX_VALUE, list);
                                }

                                @NonNull
                                @Override
                                public List<Cookie> loadForRequest(@NonNull HttpUrl httpUrl) {
                                    List<Cookie> result = cookies.get(Integer.MAX_VALUE);
                                    return result == null ? new ArrayList<>() : result;
                                }
                            })
                            .build();
                        FormBody formBody = new FormBody.Builder()
                            .add("accessKeySecret", accessKeySecret)
                            .build();

                        Request request = new Request.Builder()
                            // .url(Objects.requireNonNull(okhttpLoginUrl).newBuilder().host(ipAddress).build())
                            .addHeader("Hos     t", okhttpLoginUrl.host())
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
                                        // JsonObject jsonObject = new Gson().toJsonTree(responseData.getData()).getAsJsonObject();
                                        // String token = jsonObject.get("saToken").getAsString();
                                        // URL connectURL = new URL(loginURL, "./ws-connect?satoken=" + token);
                                        URL connectURL = new URL(loginURL, "./ws-connect");
                                        String tmpConnectURLInText = connectURL.toString();
                                        if (tmpConnectURLInText.startsWith("http")) {
                                            connectUrlInText = "ws" + tmpConnectURLInText.substring(4);
                                        } else {
                                            connectUrlInText = tmpConnectURLInText;
                                        }
                                    } else {
                                        normalNotification("WebSocket登录失败！调试消息：" + loginResponsePayload);
                                    }
                                } catch (JsonSyntaxException e) {
                                    normalNotification("WebSocket登录请求回执内容解析失败！调试消息：" + e.getMessage());
                                }
                            } else {
                                normalNotification("WebSocket登录失败！调试消息：" + loginResponse.body());
                            }
                            loginResponse.close();
                        } catch (IOException e) {
                            normalNotification("WebSocket登录请求失败！调试消息：" + e.getMessage());
                        }
                    }
                } catch (MalformedURLException e) {
                    normalNotification("WebSocket登录URL格式错误！调试消息：" + e.getMessage());
                }
            }

            if (!"".equals(connectUrlInText)) {
                // Create a WebSocketFactory instance.
                WebSocketFactory factory = new WebSocketFactory();
                if (connectUrlInText.startsWith("wss")) {
                    // Create a custom SSL context.
                    SSLContext context = null;
                    try {
                        context = SSLContext.getInstance("TLS", "AndroidOpenSSL");
                    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                        normalNotification("WebSocket 创建TLS环境失败！调试消息：" + e.getMessage());
                    }

                    // Set the custom SSL context.
                    factory.setSSLContext(context);
                }

                factory.setConnectionTimeout(10 * 1000);
                try {
                    ws = factory.createSocket(connectUrlInText);
                } catch (IOException e) {
                    normalNotification("WebSocket 初始化连接失败！调试消息：" + e.getMessage());
                }

                ws.addListener(new WebSocketAdapter() {
                    @Override
                    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                        websocketConnected = true;
                        super.onConnected(websocket, headers);
                    }

                    @Override
                    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                        websocketConnected = false;
                        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
                    }

                    @Override
                    public void onTextMessage(WebSocket websocket, String text) throws Exception {
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
                            normalNotification("收到消息，但解析发生错误。调试消息：" + e.getMessage());
                        }
                        super.onTextMessage(websocket, text);
                    }
                });

                try {
                    if (cookies.values().size() > 0) {
                        Cookie cookie = cookies.values().iterator().next().get(0);
                        ws.addHeader("Cookie", cookie.name() + '=' + cookie.value());
                    }
                    ws = ws.connect();
                    websocketInitialized = true;
                } catch (WebSocketException e) {
                    normalNotification("WebSocket 连接失败！调试消息：" + e.getMessage());
                }

            }
        }).start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy...");
        synchronized (TAG) {
            isClosing = true;
            if (websocketInitialized) {
                ws.sendClose();
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
                smSender = new SmSender(context);
                normalNotification("创建短信服务成功！");
            } catch (ReflectiveOperationException | RemoteException e) {
                normalNotification("创建短信服务失败！原因：" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            normalNotification("创建短信服务失败！原因：" + "Shizuku 未授权");
        }
    }

    private int postNotification(String title, String content) {
        int requestCode = notificationIdGenerator.generate();

        // 设置取消后的动作
        Intent intent = new Intent(CLEAR_COMMON_NOTIFICATION_ACTION);
        intent.putExtra("sequence", requestCode);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        // 初始化 notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)    // 设置标题
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

    private void normalNotification(String content) {
        NotificationUtils.postNotification(context, SmSender.NOTIFICATION_TITLE, content);
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
                reinitWsGo();
            }
        }
        if (MyApplication.SP_KEY_WEBSOCKET_SERVER_ACCESS_KEY_SECRET.equals(key)) {
            String url = sharedPreferences.getString(MyApplication.SP_KEY_WEBSOCKET_SERVER_LOGIN_URL, null);
            if (url != null) {
                reinitWsGo();
            }
        }
    }
}
