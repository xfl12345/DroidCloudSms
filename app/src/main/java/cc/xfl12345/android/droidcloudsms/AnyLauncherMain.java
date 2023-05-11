package cc.xfl12345.android.droidcloudsms;

import android.content.Context;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import cc.xfl12345.android.droidcloudsms.model.MyShizukuContext;
import cc.xfl12345.android.droidcloudsms.model.NotificationUtils;
import cc.xfl12345.android.droidcloudsms.model.SmSender;
// import per.goweii.anypermission.AnyPermission;
// import per.goweii.anypermission.RequestListener;

public class AnyLauncherMain {
    private final Context context;

    private final MyShizukuContext myShizukuContext;

    private SmSender smSender = null;


    public Context getContext() {
        return context;
    }

    public MyShizukuContext getMyShizukuContext() {
        return myShizukuContext;
    }

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

    public AnyLauncherMain(@NonNull Context context) {
        this.context = context;

        myShizukuContext = new MyShizukuContext(context);
    }


}
