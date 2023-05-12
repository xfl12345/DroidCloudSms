package cc.xfl12345.android.droidcloudsms.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentTestBinding;
import cc.xfl12345.android.droidcloudsms.model.SmContent;
import cc.xfl12345.android.droidcloudsms.model.SmSender;

public class TestFragment extends Fragment {
    private FragmentTestBinding binding;

    public TestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        binding =  FragmentTestBinding.bind(view);

        FloatingActionButton fab = binding.fab;

        fab.setOnClickListener(view1 -> {
            String phoneNumber = getStrFromEditTextById(R.id.edit_text_phone_number);
            if (phoneNumber == null || phoneNumber.equals("")) {
                phoneNumber = "10086";
            }

            SmContent smContent = new SmContent();
            smContent.setContent("测试");
            smContent.setPhoneNumber(phoneNumber);

            new Thread(() -> {
                SmSender smSender = ((MyApplication) view1.getContext().getApplicationContext()).getSmSender();
                smSender.sendMessage(smContent.getContent(), smContent.getPhoneNumber());
            }).start();
        });

        // Inflate the layout for this fragment
        return view;
    }

    public String getStrFromEditTextById(int id){
        return ((EditText) binding.getRoot().findViewById(id))
            .getText().toString();
    }
}
