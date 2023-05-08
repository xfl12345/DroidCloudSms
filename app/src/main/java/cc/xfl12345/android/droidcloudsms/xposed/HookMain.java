package cc.xfl12345.android.droidcloudsms.xposed;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.fasterxml.jackson.databind.ObjectMapper;

import cc.xfl12345.android.droidcloudsms.R;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {

    public Context miuiSmsUiContext = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        ObjectMapper objectMapper = new ObjectMapper();
        XposedBridge.log("没有更新是什么鬼？Current lpparam packageName:[" + lpparam.packageName + "] XFL's xposed module [cc.xfl12345.android.xposed.mysmssender] started!");

//        Class<?> testSmsMessageClass = XposedHelpers.findClass("com.android.mms", Thread.currentThread().getContextClassLoader());
//        XposedBridge.log(objectMapper.valueToTree(List.of(testSmsMessageClass.getDeclaredMethods())).toPrettyString());


        if (lpparam.packageName.startsWith("com.android.mms")) {
            Class<?> uiClass = XposedHelpers.findClass("com.android.mms.ui.MmsTabActivity", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(uiClass, "onCreate", Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                Activity activity = (Activity) param.thisObject;
                                Context context = activity.getApplicationContext();
                                Toast toast = Toast.makeText(context, "XFL xposed hook 成功！", Toast.LENGTH_SHORT);
                                toast.show();
                                androidNotify(context, "XFL xposed", "hook activity 成功！");
                                miuiSmsUiContext = context;

                                Class<?> mSmsMessageClass = XposedHelpers.findClass("android.telephony.SmsManager", lpparam.classLoader);
                                Toast.makeText(miuiSmsUiContext, "XFL xposed 成功获取到 SmsManager Class ！ [" + mSmsMessageClass.getCanonicalName() + "]", Toast.LENGTH_SHORT).show();

                                XposedHelpers.findAndHookMethod(mSmsMessageClass, "getSmscAddress",
                                        // String.class, String.class, String.class, PendingIntent.class, PendingIntent.class, long.class,
                                        new XC_MethodHook() {
                                            @Override
                                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                                try {
                                                    SmsManager smsManager = (SmsManager) param.thisObject;
                                                    new Thread(() -> {
                                                        try {
                                                            Thread.sleep(1000);
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                        if (miuiSmsUiContext != null) {
                                                            @SuppressLint("MissingPermission")
                                                            Toast toast = Toast.makeText(miuiSmsUiContext, "XFL xposed 成功获取到 SmsManager ！ [" + smsManager.getClass().getCanonicalName() + "]", Toast.LENGTH_SHORT);
                                                            toast.show();
                                                        }
                                                    }).start();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                            } catch (Exception e) {
                                Log.e("xflsXposedMod", e.getMessage(), e);
                            }
                        }
                    });

        } else if (lpparam.packageName.startsWith("top.yzzblog.messagehelper")) {
            Class<?> mSmsMessageClass = XposedHelpers.findClass("top.yzzblog.messagehelper.fragments.HomeFrag", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(mSmsMessageClass, "onAttach", Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                Context context = (Context) param.args[0];
                                Toast toast = Toast.makeText(context, "XFL xposed hook 成功！", Toast.LENGTH_SHORT);
                                toast.show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } else if (lpparam.packageName.startsWith("android.telephony") || lpparam.packageName.startsWith("com.moez.QKSMS")) {

            Class<?> mSmsMessageClass = XposedHelpers.findClass("android.telephony.SmsManager", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(mSmsMessageClass, "getCarrierConfigValues",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                SmsManager smsManager = (SmsManager) param.thisObject;
//                                smsManager.sendTextMessage();
//                                Context context = (Context) param.args[0];
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(10000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (miuiSmsUiContext != null) {
                                        @SuppressLint("MissingPermission")
                                        Toast toast = Toast.makeText(miuiSmsUiContext, "XFL xposed 成功获取到 SmsManager ！ [" + smsManager.getClass().getCanonicalName() + "]", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                }).start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

//            @SuppressWarnings("unchecked")
//            Class<SmsManager> mSmsMessageClass = (Class<SmsManager>) XposedHelpers.findClass("android.telephony.SmsManager", lpparam.classLoader);
//            XposedHelpers.findAndHookMethod(mSmsMessageClass, "getCarrierConfigValues",
//                    new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            try {
//                                SmsManager smsManager = (SmsManager) param.thisObject;
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });


//            XposedHelpers.findAndHookConstructor(mSmsMessageClass, new XC_MethodHook() {
//                public void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    Log.d ("xfl12345.xp.sms", "这是无参构造函数前");
//                }
//
//                public void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    Log.d ("xfl12345.xp.sms", "这是无参构造函数后");
//                    SmsManager smsManager = (SmsManager) param.thisObject;
////                    smsManager.sendTextMessage();
////                    XposedHelpers.setIntField(param.thisObject, "publicInt", 20000000);
//                }
//
//            });
        }

//        Class<?> mSmsMessageClass = XposedHelpers.findClass("com.android.internal.telephony.gsm.SmsMessage", Thread.currentThread().getContextClassLoader());
//        XposedHelpers.findAndHookMethod(mSmsMessageClass, "createFromPdu", byte[].class,
//                new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        try {
//                            Object smsMessage = param.getResult();
//                            if (null != smsMessage) {
//                                String from = (String) XposedHelpers.callMethod(smsMessage, "getOriginatingAddress");
//                                String msgBody = (String) XposedHelpers.callMethod(smsMessage, "getMessageBody");
//                                XposedBridge.log("test_sms 收到短信---->" + "from:" + from + " msgBody:" + msgBody);
//
//
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            XposedBridge.log("SMS listen error=" + e.getMessage());
//                        }
//
//                    }
//                });

//        if (lpparam.packageName.equals("com.android.mms")) {
//
//        }


    }


    public void androidNotify(Context context, String title, String content) {
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {   //版本大于等于 安卓8.0
            NotificationChannel channel = new NotificationChannel("leo", "测试通知", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        // 设置取消后的动作
        Intent intent = new Intent();
        intent.setClass(context, context.getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        //初始化 notification
        Notification notification = new NotificationCompat.Builder(context, "leo")
                .setContentTitle(title)    //设置标题
                .setContentText(content)    //设置通知文字
                .setSmallIcon(R.drawable.ic_launcher_foreground)   //设置左边的小图标
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_foreground))  //设置大图标
                .setColor(Color.parseColor("#ff0000"))
                .setContentIntent(pendingIntent)  // 设置点击通知之后 进入相关页面(此处进入NotificationActivity类，执行oncreat方法打印日志)
                .setAutoCancel(true)   //设置点击通知后 通知通知栏不显示
                .build();

        manager.notify(1, notification);
    }

}
