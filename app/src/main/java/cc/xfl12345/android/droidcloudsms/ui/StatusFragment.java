package cc.xfl12345.android.droidcloudsms.ui;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.WebsocketService;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentStatusBinding;
import cc.xfl12345.android.droidcloudsms.model.WebSocketServiceConnectionListener;

public class StatusFragment extends Fragment implements WebSocketServiceConnectionListener {

    private MyApplication context;

    private FragmentStatusBinding binding;

    private WebsocketService websocketService = null;

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
        context = ((MyApplication) requireContext().getApplicationContext());
        binding = FragmentStatusBinding.bind(view);

        refreshView();

        return view;
    }

    private void refreshView() {
        boolean connected2WebsocketService = websocketService != null;

        boolean smsStandby = connected2WebsocketService && websocketService.isSmsReady();
        binding.smsStatusMonitor.setText(smsStandby ? "正常工作" : "未工作");
        binding.smsStatusMonitor.setTextColor(smsStandby ? Color.GREEN : Color.RED);

        boolean wsStandby = connected2WebsocketService && websocketService.isWebSocketConnected();
        binding.wsStatusMonitor.setText(wsStandby ? "已连接" : "未连接");
        binding.wsStatusMonitor.setTextColor(wsStandby ? Color.GREEN : Color.RED);
    }

    @Override
    public void onResume() {
        context.addWebSocketServiceConnectionListener(this);
        if (websocketService == null) {
            refreshView();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        context.removeWebSocketServiceConnectionListener(this);
        super.onPause();
    }

    @Override
    public void onServiceConnected(WebsocketService service) {
        websocketService = service;
        refreshView();
    }

    @Override
    public void onServiceDisconnected() {
        websocketService = null;
    }
}