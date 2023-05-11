package cc.xfl12345.android.droidcloudsms.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.hjq.permissions.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentAndroidPermissionManagerBinding;

public class AndroidPermissionManagerFragment extends Fragment {

    private FragmentAndroidPermissionManagerBinding binding;

    private ListView listView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_android_permission_manager, container, false);
        binding = FragmentAndroidPermissionManagerBinding.bind(view);

        List<Map.Entry<String, String>> entryList = new ArrayList<>(2);
        entryList.add(Map.entry(Permission.NOTIFICATION_SERVICE, "通知栏权限"));
        entryList.add(Map.entry(Permission.POST_NOTIFICATIONS, "发送通知权限"));
        entryList.add(Map.entry(Permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "忽略电池优化选项权限"));

        listView = binding.myList;
        listView.setAdapter(new AndroidPermissionListAdapter(requireContext(), entryList));
        return view;
    }
}
