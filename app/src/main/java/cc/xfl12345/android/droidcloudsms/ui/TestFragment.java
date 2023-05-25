package cc.xfl12345.android.droidcloudsms.ui;

import static android.content.Context.BIND_AUTO_CREATE;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.WebsocketService;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentTestBinding;
import cc.xfl12345.android.droidcloudsms.model.NotificationUtils;
import cc.xfl12345.android.droidcloudsms.model.SmContent;
import cc.xfl12345.android.droidcloudsms.model.SmSender;

public class TestFragment extends Fragment {
    private FragmentTestBinding binding;

    private Context context;

    private boolean isBindService = false;

    private boolean isServiceConnected = false;

    private WebsocketService.WebsocketServiceBinder binder;

    private WebsocketService service = null;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            isServiceConnected = true;
            binder = (WebsocketService.WebsocketServiceBinder) iBinder;
            service = binder.getService();
            NotificationUtils.postNotification(context, "短信服务已连接", "APP已成功连接上短信服务");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceConnected = false;
            NotificationUtils.postNotification(context, "短信服务已断开", "短信服务已断开");
        }
    };

    public TestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        Intent bindIntent = new Intent(context, WebsocketService.class);
        isBindService = context.bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        binding =  FragmentTestBinding.bind(view);

        FloatingActionButton fab = binding.fab;

        fab.setOnClickListener(view1 -> {
            String phoneNumber = binding.editTextPhoneNumber.getText().toString();
            if (phoneNumber == null || phoneNumber.equals("")) {
                phoneNumber = "10086";
            }

            SmContent smContent = new SmContent();
            smContent.setContent("测试");
            smContent.setPhoneNumber(phoneNumber);

            new Thread(() -> {
                if (isServiceConnected) {
                    SmSender smSender = service.getSmSender();
                    smSender.sendMessage(smContent.getContent(), smContent.getPhoneNumber());
                } else {
                    NotificationUtils.postNotification(context, "测试发送短信失败", "短信服务未连接");
                }
            }).start();
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onDestroy() {
        if (isBindService) {
            context.unbindService(serviceConnection);
        }
        super.onDestroy();
    }
}
