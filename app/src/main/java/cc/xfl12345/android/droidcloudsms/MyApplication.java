package cc.xfl12345.android.droidcloudsms;

import android.app.Application;

public class MyApplication extends Application {
    private AnyLauncherMain anyLauncherMain;

    @Override
    public void onCreate() {
        super.onCreate();
        anyLauncherMain = new AnyLauncherMain(getApplicationContext());
    }

    public AnyLauncherMain getAnyLaucherMain() {
        return anyLauncherMain;
    }

    @Override
    public void onTerminate() {

        super.onTerminate();
    }
}
