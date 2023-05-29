package cc.xfl12345.android.droidcloudsms.ui;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentStatusBinding;

public class StatusFragment extends Fragment {

    private MyApplication context;

    public StatusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        FragmentStatusBinding binding = FragmentStatusBinding.bind(view);
        context = ((MyApplication) requireContext().getApplicationContext());

        boolean connected2WebsocketService = context.isConnected2WebsocketService();

        boolean smsStandby = connected2WebsocketService &&
            context.getWebsocketService().isSmsReady();
        binding.smsStatusMonitor.setText(smsStandby ? "正常工作" : "未工作");
        binding.smsStatusMonitor.setTextColor(smsStandby ? Color.GREEN : Color.RED);

        boolean wsStandby = connected2WebsocketService &&
            context.getWebsocketService().isWebsocketConnected();
        binding.wsStatusMonitor.setText(wsStandby ? "已连接" : "未连接");
        binding.wsStatusMonitor.setTextColor(wsStandby ? Color.GREEN : Color.RED);

        return view;
    }
}