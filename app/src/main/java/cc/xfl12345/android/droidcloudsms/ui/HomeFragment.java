package cc.xfl12345.android.droidcloudsms.ui;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;

import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.WebsocketService;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private MyApplication context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (MyApplication) requireContext().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        FragmentHomeBinding binding = FragmentHomeBinding.bind(view);

        MaterialButton button = binding.fragmentHomeButton;
        button.setOnClickListener(v -> {
            button.setBackgroundColor(Color.YELLOW);
            button.setIcon(AppCompatResources.getDrawable(context, R.drawable.baseline_360_24));
            button.setText("正在重新初始化WebSocket");
            new Thread(() -> {
                try {
                    Thread thread = context.getWebsocketService().reinitWebSocket();
                    thread.join();
                    updateButton(button);
                } catch (Exception e) {
                    // ignore
                }
            }).start();
        });

        updateButton(button);

        return view;
    }

    public boolean isAllOK() {
        boolean flag = false;
        if (context.isConnected2WebsocketService()) {
            WebsocketService websocketService = context.getWebsocketService();
            flag = websocketService.isSmsReady() && websocketService.isWebsocketConnected();
        }

        return flag;
    }

    public void updateButton(MaterialButton button) {
        button.post(() -> {
            context = ((MyApplication) button.getContext().getApplicationContext());

            boolean ok = isAllOK();
            button.setBackgroundColor(ok ? Color.GREEN : Color.RED);
            button.setIcon(AppCompatResources.getDrawable(
                context,
                ok ? R.drawable.baseline_check_24 : R.drawable.baseline_close_24
            ));
            button.setText(ok ? "工作正常" : "未工作");
        });
    }
}
