package cc.xfl12345.android.droidcloudsms;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.gnepux.wsgo.EventListener;
import com.gnepux.wsgo.WsConfig;
import com.gnepux.wsgo.WsGo;
import com.gnepux.wsgo.jwebsocket.JWebSocket;

import java.io.IOException;

import cc.xfl12345.android.droidcloudsms.model.NotificationUtils;
import cc.xfl12345.android.droidcloudsms.model.SmSender;

// source code URL=https://blog.csdn.net/yxl930401/article/details/127963284
// source code URL=https://blog.csdn.net/weixin_35691921/article/details/124419935
public class WebsocketService extends Service {
    private static final String TAG = "DroidCloudSmsWebSocketService";

    public static final String SERVICE_NAME = TAG;

    public static final String SERVICE_NOTIFICATION_NAME = "SmsWebsocket";

    public static final int SERVICE_ID_INT = 1;

    private final IBinder binder = new WebsocketServiceBinder();

    private MyApplication context;

    private SmSender smSender = null;

    public SmSender getSmSender() {
        if (smSender == null) {
            if (context.getMyShizukuContext().requirePermission()) {
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

    public WebsocketService() {
    }

    //用于Activity和service通讯
    public class WebsocketServiceBinder extends Binder {
        public WebsocketService getService() {
            return WebsocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = (MyApplication) getApplicationContext();
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
                NotificationChannel channel = new NotificationChannel(SERVICE_NAME, SERVICE_NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH);
                // 启用震动
                channel.enableVibration(true);
                // 渐进式震动（先震动 0.8 秒，然后停止 0.5 秒，再震动 1.2 秒）
                channel.setVibrationPattern(new long[] {800, 500, 1200});
                notificationManager.createNotificationChannel(channel);
                Notification notification = new Notification.Builder(this, SERVICE_NAME).setOngoing(true).build();
                startForeground(SERVICE_ID_INT, notification);
            }
        }

        reinitWsGo();

        Log.i(TAG, "onCreate...");
    }


    public void reinitWsGo() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(MyApplication.SP_KEY_APP_CONFIG, MODE_PRIVATE);
        String websocketServerURL = sharedPreferences.getString(MyApplication.SP_KEY_WEBSOCKET_SERVER_URL, "");

        if (!"".equals(websocketServerURL)) {
            WsConfig config = new WsConfig.Builder()
                .debugMode(true)    // true to print log
                .setUrl(websocketServerURL)    // ws url
                .setConnectTimeout(10 * 1000L)  // connect timeout
                .setReadTimeout(10 * 1000L)     // read timeout
                .setWriteTimeout(10 * 1000L)    // write timeout
                .setPingInterval(10 * 1000L)    // initial ping interval
                .setWebSocket(JWebSocket.create()) // websocket client
                .setRetryStrategy(retryCount -> 0)    // retry count and delay time strategy
                .setEventListener(new EventListener() {
                    @Override
                    public void onConnect() {

                    }

                    @Override
                    public void onDisConnect(Throwable throwable) {

                    }

                    @Override
                    public void onClose(int code, String reason) {

                    }

                    @Override
                    public void onMessage(String text) {

                    }

                    @Override
                    public void onReconnect(long retryCount, long delayMillSec) {

                    }

                    @Override
                    public void onSend(String text, boolean success) {

                    }
                })    // event listener
                .build();
            WsGo.init(config);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy...");

        try {
            WsGo.getInstance().destroyInstance();
        } catch (Exception e) {
            // ignore
        }

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

}
