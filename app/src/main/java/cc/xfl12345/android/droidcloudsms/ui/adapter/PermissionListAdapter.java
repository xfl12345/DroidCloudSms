package cc.xfl12345.android.droidcloudsms.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.iielse.switchbutton.SwitchView;

import java.util.List;
import java.util.function.BiConsumer;

import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.model.PermissionItem;

public class PermissionListAdapter extends BaseAdapter {

    private Activity activity;

    private List<PermissionItem> dataList;

    private BiConsumer<String, Boolean> afterButtonClicked = (permissionName, granted) -> {};

    public void setAfterButtonClickedListener(BiConsumer<String, Boolean> afterButtonClicked) {
        this.afterButtonClicked = afterButtonClicked;
    }

    public PermissionListAdapter(Activity activity, List<PermissionItem> dataList) {
        this.activity = activity;
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
        // String permissionName = dataList.get(position).getKey();
        // return isGranted(permissionName) ? (permissionName + ":true").hashCode() : (permissionName + ":false").hashCode();
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PermissionItem dataItem = dataList.get(position);
        String permissionName = dataItem.getCodeName();
        String displayName = dataItem.getDisplayName();

        if(convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.permission_item, null);
        }


        TextView textView = convertView.findViewById(R.id.permissionName);
        textView.setText(displayName);

        SwitchView switchButton = convertView.findViewById(R.id.permissionSwitchButton);
        switchButton.setOpened(dataItem.isGranted());
        switchButton.setEnabled(true);
        switchButton.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View view) {
                SwitchView button = (SwitchView) view;
                button.setOpened(dataItem.isGranted());
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {}
        });

        dataItem.setRequestPermissionCallback((beforeRequestStatus, afterRequestStatus, targetStatus) -> {
            switchButton.post(() -> {
                switchButton.toggleSwitch(afterRequestStatus);
                afterButtonClicked.accept(permissionName, afterRequestStatus);
            });
        });

        switchButton.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {
                new Thread(() -> {
                    dataItem.requestPermission(view.isOpened(), true);
                }).start();
            }

            @Override
            public void toggleToOff(SwitchView view) {
                new Thread(() -> {
                    dataItem.requestPermission(view.isOpened(), false);
                }).start();
            }
        });

        // ConstraintLayout layout = convertView.findViewById(R.id.item_container);
        // layout.setOnClickListener(v -> {
        //     new Thread(()-> {
        //         layout.post(() -> v.findViewById(R.id.permissionSwitchButton).performClick());
        //     }).start();
        // });

        return convertView;
    }

}
