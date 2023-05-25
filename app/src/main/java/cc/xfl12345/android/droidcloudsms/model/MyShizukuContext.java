package cc.xfl12345.android.droidcloudsms.model;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import rikka.shizuku.Shizuku;

public class MyShizukuContext implements
        Shizuku.OnBinderDeadListener,
        Shizuku.OnBinderReceivedListener,
        Shizuku.OnRequestPermissionResultListener,
        Closeable {
    public static final String TAG = "shizuku";

    private final Context context;

    private volatile boolean granted = false;

    public boolean isGranted() {
        return granted;
    }

    private volatile boolean shizukuServiceConnected = false;

    public boolean isShizukuServiceConnected() {
        return shizukuServiceConnected;
    }

    private final IdGenerator idGenerator = new IdGenerator(0);

    private final Map<String, SynchronizeLock> synchronizeLocks = new ConcurrentHashMap<>();


    public MyShizukuContext(Context context) {
        this.context = context;
        refreshPermissionStatus();
        Shizuku.addBinderReceivedListener(this);
        Shizuku.addBinderDeadListener(this);
    }

    @Override
    public void onBinderDead() {
        shizukuServiceConnected = false;
    }

    @Override
    public void onBinderReceived() {
        shizukuServiceConnected = true;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        granted = grantResult == PackageManager.PERMISSION_GRANTED;
        // NotificationUtils.postNotification(context, "Shizuku", granted ? "已获得 Shizuku 授权" : "Shizuku 拒绝授权");
        try {
            String requestCodeString = "" + requestCode;
            SynchronizeLock lock = synchronizeLocks.putIfAbsent(requestCodeString, createSynLock());
            if (lock == null) {
                lock = synchronizeLocks.get(requestCodeString);
            }
            if (lock != null) {
                lock.justSynchronize();
                synchronizeLocks.remove(requestCodeString);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private SynchronizeLock createSynLock() {
        return new SynchronizeLock(30000, 0);
    }

    public boolean requirePermission() {
        if (refreshPermissionStatus()) {
            return true;
        }

        boolean result = false;
        if (!Shizuku.shouldShowRequestPermissionRationale()) {
            Shizuku.addRequestPermissionResultListener(this);

            SynchronizeLock lock = createSynLock();
            int requestCodeInt = idGenerator.generate();
            String requestCodeString;
            synchronized (TAG) {
                requestCodeString = "" + requestCodeInt;
                while (synchronizeLocks.containsKey(requestCodeString)) {
                    requestCodeInt = idGenerator.generate();
                    requestCodeString = "" + requestCodeInt;
                }

                synchronizeLocks.put(requestCodeString, lock);
            }

            Shizuku.requestPermission(requestCodeInt);
            try {
                lock.justSynchronize();
                synchronizeLocks.remove(requestCodeString);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                Shizuku.removeRequestPermissionResultListener(this);
            }

            result = granted;
        } else {
            NotificationUtils.postNotification(context, "Shizuku", "Shizuku 拒绝授权并且不再询问");
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

    @Override
    public void close() throws IOException {
        Shizuku.removeBinderReceivedListener(this);
        Shizuku.removeBinderDeadListener(this);
    }
}
