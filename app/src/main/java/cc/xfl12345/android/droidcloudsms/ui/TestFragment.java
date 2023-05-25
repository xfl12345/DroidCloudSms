package cc.xfl12345.android.droidcloudsms.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentTestBinding;
import cc.xfl12345.android.droidcloudsms.model.NotificationUtils;
import cc.xfl12345.android.droidcloudsms.model.SmContent;
import cc.xfl12345.android.droidcloudsms.model.SmSender;

public class TestFragment extends Fragment {
    private FragmentTestBinding binding;

    private MyApplication context;

    public TestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (MyApplication) requireContext().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        binding =  FragmentTestBinding.bind(view);

        FloatingActionButton fab = binding.fab;

        fab.setOnClickListener(view1 -> {
            String phoneNumber = binding.editTextPhoneNumber.getText().toString();
            if ("".equals(phoneNumber)) {
                phoneNumber = "10086";
            }

            SmContent smContent = new SmContent();
            smContent.setContent("测试");
            smContent.setPhoneNumber(phoneNumber);

            new Thread(() -> {
                if (context.isConnected2WebsocketService() && context.getWebsocketService().isSmsReady()) {
                    SmSender smSender = context.getWebsocketService().getSmSender();
                    smSender.sendMessage(smContent.getContent(), smContent.getPhoneNumber());
                } else {
                    NotificationUtils.postNotification(context, "测试发送短信失败", "短信服务未工作");
                }
            }).start();
        });

        // Inflate the layout for this fragment
        return view;
    }

}
