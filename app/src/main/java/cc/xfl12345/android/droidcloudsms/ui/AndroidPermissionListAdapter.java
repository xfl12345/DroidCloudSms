package cc.xfl12345.android.droidcloudsms.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.iielse.switchbutton.SwitchView;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import cc.xfl12345.android.droidcloudsms.R;

public class AndroidPermissionListAdapter extends BaseAdapter {

    private Context context;

    private List<Map.Entry<String, String>> dataList;

    public AndroidPermissionListAdapter(Context context, List<Map.Entry<String, String>> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map.Entry<String, String> dataItem = dataList.get(position);
        String permissionName = dataItem.getKey();
        String displayName = dataItem.getValue();

        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.android_permission_item, null);
        }

        TextView textView = convertView.findViewById(R.id.permissionName);
        textView.setText(displayName);

        SwitchView switchButton = convertView.findViewById(R.id.permissionSwitchButton);
        switchButton.setOpened(isGranted(permissionName));
        switchButton.setEnabled(true);
        switchButton.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View view) {
                SwitchView button = (SwitchView) view;
                button.setOpened(isGranted(permissionName));
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {}
        });

        switchButton.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {
                beforeSwitchStateChanged(permissionName, view.isOpened(), view);
            }

            @Override
            public void toggleToOff(SwitchView view) {
                beforeSwitchStateChanged(permissionName, view.isOpened(), view);
            }
        });

        return convertView;
    }

    protected void beforeSwitchStateChanged(String permissionName, boolean isChecked, View view) {
        XXPermissions.with(context)
            .permission(permissionName)
            .request(new OnPermissionCallback() {
                @Override
                public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                    ((SwitchView) view).toggleSwitch(true);
                }

                @Override
                public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                    ((SwitchView) view).toggleSwitch(false);
                }
            });
    }

    protected boolean isGranted(String permission) {
        return XXPermissions.isGranted(context, permission);
    }

}
