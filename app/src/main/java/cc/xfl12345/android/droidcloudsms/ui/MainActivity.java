package cc.xfl12345.android.droidcloudsms.ui;

import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.EditText;

import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.model.MyShizukuContext;
import cc.xfl12345.android.droidcloudsms.databinding.ActivityMainBinding;
import cc.xfl12345.android.droidcloudsms.model.SmsContent;
import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private MyShizukuContext myShizukuContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle(getTitle());

        FloatingActionButton fab = binding.fab;


        myShizukuContext = new MyShizukuContext(getApplicationContext());

        myShizukuContext.refreshPermissionStatus();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = getStrFromEditTextById(R.id.edit_text_phone_number);
                if (phoneNumber == null || phoneNumber.equals("")) {
                    phoneNumber = "10086";
                }

                SmsContent smsContent = new SmsContent();
                smsContent.setContent("测试");
                smsContent.setPhoneNumber(phoneNumber);

                if (myShizukuContext.requirePermission()) {
                    for (int i = 0; i < 10; i++) {
                        Log.i("cc.xfl12345.android.xposed.mysmssender", "Current UID=" + Shizuku.getUid());
                    }

                    myShizukuContext.testSendSms(smsContent);
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    public String getStrFromEditTextById(int id){
        return ((EditText) binding.getRoot().findViewById(id))
            .getText().toString();
    }
}
