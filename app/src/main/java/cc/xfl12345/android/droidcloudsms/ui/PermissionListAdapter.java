package cc.xfl12345.android.droidcloudsms.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.iielse.switchbutton.SwitchView;

import java.util.List;
import java.util.function.BiConsumer;

import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.model.PermissionItem;

public class PermissionListAdapter extends BaseAdapter {

    private Context context;

    private List<PermissionItem> dataList;

    private BiConsumer<String, Boolean> afterButtonClicked = (permissionName, granted) -> {};

    public void setAfterButtonClickedListener(BiConsumer<String, Boolean> afterButtonClicked) {
        this.afterButtonClicked = afterButtonClicked;
    }

    public PermissionListAdapter(Context context, List<PermissionItem> dataList) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.permission_item, null);
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
            switchButton.toggleSwitch(afterRequestStatus);
            afterButtonClicked.accept(permissionName, afterRequestStatus);
        });

        switchButton.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(SwitchView view) {
                dataItem.requestPermission(view.isOpened(), true);
            }

            @Override
            public void toggleToOff(SwitchView view) {
                dataItem.requestPermission(view.isOpened(), false);
            }
        });

        // ConstraintLayout layout = convertView.findViewById(R.id.item_container);
        // layout.setOnClickListener(v -> {
        //     v.findViewById(R.id.permissionSwitchButton).performClick();
        // });

        return convertView;
    }

}
