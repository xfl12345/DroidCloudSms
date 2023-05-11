package cc.xfl12345.android.droidcloudsms.model;

import android.app.Activity;

import java.lang.ref.WeakReference;

public class MyActivityManager {
    private static WeakReference<Activity> currentActivity = null;

    public static void setCurrentActivity(Activity activity) {
        currentActivity = new WeakReference<>(activity);
    }

    public static Activity getCurrentActivity() {
        return currentActivity == null ? null : currentActivity.get();
    }
}
