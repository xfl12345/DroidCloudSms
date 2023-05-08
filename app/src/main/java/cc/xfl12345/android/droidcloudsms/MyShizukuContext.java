package cc.xfl12345.android.droidcloudsms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class MyShizukuContext implements
        Shizuku.OnBinderDeadListener,
        Shizuku.OnBinderReceivedListener,
        Shizuku.OnRequestPermissionResultListener {
    public static final String TAG = "shizuku";

    private Context context;

    public static int REQUEST_CODE = 888;

    private boolean granted = false;

    private final ThreadLocal<SynchronizeLock> synchronizeLockThreadLocal = new ThreadLocal<>();

    IPackageManager packageManager;


    public boolean isGranted() {
        return granted;
    }

    public MyShizukuContext(Context context) {
        this.context = context;
    }

    public void initService() {
        Method mPackageManagerGetPackagesForUid;
        Object iPmInstance;

        try {
            ShizukuBinderWrapper shizukuBinderWrapper = new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package"));
            Log.i(TAG, shizukuBinderWrapper.getInterfaceDescriptor());
            packageManager = IPackageManager.Stub.asInterface(shizukuBinderWrapper);
            int packageUid = 10051;
            Log.i(TAG, "Package name of UID [" + packageUid + "] = " + packageManager.getPackagesForUid(packageUid)[0]);

            Class<?> iPmClass = Class.forName("android.content.pm.IPackageManager");
            @SuppressLint("PrivateApi")
            Class<?> iPmStub = Class.forName("android.content.pm.IPackageManager$Stub");
            Method asInterfaceMethod = iPmStub.getMethod("asInterface", IBinder.class);
            iPmInstance = asInterfaceMethod.invoke(null, new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")));

            mPackageManagerGetPackagesForUid = iPmClass.getMethod("getPackagesForUid", int.class);
            Log.i(TAG, "Package name of UID [" + packageUid + "] = " + ((String[]) mPackageManagerGetPackagesForUid.invoke(iPmInstance, packageUid))[0]);


            SmsSender smsSender = new SmsSender(context);
            smsSender.sendMessage("测试", "10001", 1);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onBinderDead() {

    }

    @Override
    public void onBinderReceived() {

    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        if (requestCode == REQUEST_CODE) {
            granted = grantResult == PackageManager.PERMISSION_GRANTED;
            Toast.makeText(context, granted ? "已获得 Shizuku 授权" : "Shizuku 拒绝授权", Toast.LENGTH_LONG).show();
            SynchronizeLock lock = synchronizeLockThreadLocal.get();
            if (lock != null) {
                try {
                    lock.justSynchronize();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private SynchronizeLock createSynLock() {
        return new SynchronizeLock(30, 0);
    }

    public boolean requirePermission() {
        if (refreshPermissionStatus()) {
            return true;
        }

        boolean result = false;
        if (!Shizuku.shouldShowRequestPermissionRationale()) {
            Shizuku.addRequestPermissionResultListener(this);
            Shizuku.requestPermission(REQUEST_CODE);
            SynchronizeLock lock = createSynLock();
            synchronizeLockThreadLocal.set(lock);
            try {
                lock.justSynchronize();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronizeLockThreadLocal.remove();
            result = granted;
            Shizuku.removeRequestPermissionResultListener(this);
        }

        return result;
    }

    public boolean refreshPermissionStatus() {
        boolean result = !Shizuku.isPreV11() && // Pre-v11 is unsupported
                !Shizuku.shouldShowRequestPermissionRationale() && // Users choose "Deny and don't ask again"
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;

        granted = result;
        return result;
    }
}
