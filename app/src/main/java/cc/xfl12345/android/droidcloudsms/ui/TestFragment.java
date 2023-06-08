package cc.xfl12345.android.droidcloudsms.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.WebsocketService;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentTestBinding;
import cc.xfl12345.android.droidcloudsms.model.NotificationUtils;
import cc.xfl12345.android.droidcloudsms.model.SmSender;
import cc.xfl12345.android.droidcloudsms.model.TimeUtils;
import cc.xfl12345.android.droidcloudsms.model.WebSocketServiceConnectionListener;
import cc.xfl12345.android.droidcloudsms.model.ws.SmsTask;

public class TestFragment extends Fragment implements WebSocketServiceConnectionListener {
    private FragmentTestBinding binding;

    private MyApplication context;

    private WebsocketService websocketService = null;

    public TestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (MyApplication) requireContext().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        binding =  FragmentTestBinding.bind(view);

        binding.fab.setOnClickListener(view1 -> {
            String phoneNumber = binding.editTextPhoneNumber.getText().toString();
            if ("".equals(phoneNumber)) {
                phoneNumber = "10086";
            }

            String finalPhoneNumber = phoneNumber;
            new Thread(() -> {
                if (websocketService != null && websocketService.isSmsReady()) {
                    SmSender smSender = websocketService.getSmSender();
                    // smSender.getSmsManager().getMyISub().get
                    SmsTask smsTask = new SmsTask();
                    smsTask.setCreateTime(TimeUtils.getNowTimeTextInISO8601());
                    smsTask.setPhoneNumber(finalPhoneNumber);
                    smsTask.setValidationCode("888");
                    smsTask.setSmsContent("测试");
                    smSender.sendMessage(smsTask);
                } else {
                    NotificationUtils.postNotification(context, "测试发送短信失败", "短信服务未工作");
                }
            }, TestFragment.class.getName() + "_sending_message").start();
        });

        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onResume() {
        context.addWebSocketServiceConnectionListener(this);
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
    }

    @Override
    public void onServiceDisconnected() {
        websocketService = null;
    }

}
