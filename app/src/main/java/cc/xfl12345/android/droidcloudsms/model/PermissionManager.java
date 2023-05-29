package cc.xfl12345.android.droidcloudsms.model;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PermissionManager {
    protected Activity activity;

    protected List<PermissionItem> permissionList;

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(@NonNull Activity activity) {
        this.activity = activity;
    }

    public List<PermissionItem> getPermissionList() {
        return Collections.unmodifiableList(permissionList);
    }

    private PermissionManager(@NonNull Activity activity, List<PermissionItem> permissionList) {
        this.activity = activity;
        this.permissionList = permissionList;
    }

    public static class Builder {

        private final PermissionManager permissionManager;

        private final List<PermissionItem> permissionList;

        public Builder(@NonNull Activity activity) {
            this.permissionList = new CopyOnWriteArrayList<>();
            this.permissionManager = new PermissionManager(activity, permissionList);
        }

        public Builder withShizuku(MyShizukuContext myShizukuContext) {
            // 添加 Shizuku 权限选项
            permissionList.add(new PermissionItem() {
                @Override
                public String getDisplayName() {
                    return "Shizuku";
                }

                @Override
                public String getCodeName() {
                    return "Shizuku";
                }

                @Override
                public boolean isGranted() {
                    return myShizukuContext.isGranted();
                }

                @Override
                public void requestPermission(boolean beforeRequestStatus, boolean targetStatus) {
                    if (targetStatus) {
                        getRequestPermissionCallback().callback(
                            beforeRequestStatus,
                            myShizukuContext.requirePermission(),
                            targetStatus
                        );
                    } else {
                        // 暂时还不支持撤销授权，这种操作
                        getRequestPermissionCallback().callback(
                            beforeRequestStatus,
                            isGranted(),
                            targetStatus
                        );
                    }
                }
            });

            return this;
        }

        public Builder withPermission(AndroidPermissionNamePair namePair) {
            permissionList.add(new PermissionItem() {
                @Override
                public String getDisplayName() {
                    return namePair.getDisplayName();
                }

                @Override
                public String getCodeName() {
                    return namePair.getCodeName();
                }

                @Override
                public void requestPermission(boolean beforeRequestStatus, boolean targetStatus) {
                    if (targetStatus) {
                        XXPermissions.with(permissionManager.getActivity())
                            .permission(getCodeName())
                            .request(new OnPermissionCallback() {
                                @Override
                                public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                    getRequestPermissionCallback().callback(
                                        beforeRequestStatus,
                                        true,
                                        targetStatus
                                    );
                                }

                                @Override
                                public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                    getRequestPermissionCallback().callback(
                                        beforeRequestStatus,
                                        false,
                                        targetStatus
                                    );
                                }
                            });
                    } else {
                        // 暂时还不支持撤销授权，这种操作
                        getRequestPermissionCallback().callback(
                            beforeRequestStatus,
                            isGranted(),
                            targetStatus
                        );
                    }
                }

                @Override
                public boolean isGranted() {
                    return XXPermissions.isGranted(permissionManager.getActivity(), getCodeName());
                }
            });

            return this;
        }

        public Builder withPermissions(List<AndroidPermissionNamePair> namePair) {
            namePair.parallelStream().forEachOrdered(this::withPermission);
            return this;
        }

        public PermissionManager build() {
            return permissionManager;
        }
    }

    public boolean isAllPermissionGranted() {
        boolean isAllGranted = true;
        // 遍历检查全部权限情况
        for (PermissionItem item : permissionList) {
            if (!item.isGranted()) {
                isAllGranted = false;
                break;
            }
        }

        return isAllGranted;
    }

}
