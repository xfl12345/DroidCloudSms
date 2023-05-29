package cc.xfl12345.android.droidcloudsms.model;

import android.app.Activity;

public abstract class PermissionItem {

    private RequestPermissionCallback requestPermissionCallback = (beforeRequestStatus, afterRequestStatus, targetStatus) -> {};

    public abstract String getDisplayName();

    public abstract String getCodeName();

    public RequestPermissionCallback getRequestPermissionCallback() {
        return requestPermissionCallback;
    }

    public void setRequestPermissionCallback(RequestPermissionCallback requestPermissionCallback) {
        this.requestPermissionCallback = requestPermissionCallback;
    }

    public abstract void requestPermission(boolean beforeRequestStatus, boolean targetStatus);

    public abstract boolean isGranted();

    public interface RequestPermissionCallback {
        void callback(boolean beforeRequestStatus, boolean afterRequestStatus, boolean targetStatus);
    }
}
