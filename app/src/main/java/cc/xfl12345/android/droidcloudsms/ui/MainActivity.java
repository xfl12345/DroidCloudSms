package cc.xfl12345.android.droidcloudsms.ui;

import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.List;

import cc.xfl12345.android.droidcloudsms.AnyLauncherMain;
import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.model.MyShizukuContext;
import cc.xfl12345.android.droidcloudsms.databinding.ActivityMainBinding;
import cc.xfl12345.android.droidcloudsms.model.SmContent;
import cc.xfl12345.android.droidcloudsms.model.SmSender;
import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = getStrFromEditTextById(R.id.edit_text_phone_number);
                if (phoneNumber == null || phoneNumber.equals("")) {
                    phoneNumber = "10086";
                }

                SmContent smContent = new SmContent();
                smContent.setContent("测试");
                smContent.setPhoneNumber(phoneNumber);

                new Thread(() -> {
                    AnyLauncherMain anyLauncherMain = ((MyApplication) getApplication()).getAnyLaucherMain();
                    SmSender smSender = anyLauncherMain.getSmSender();
                    smSender.sendMessage(smContent.getContent(), smContent.getPhoneNumber());
                }).start();
            }
        });

        XXPermissions.with(this)
            .permission(Permission.NOTIFICATION_SERVICE)
            .permission(Permission.POST_NOTIFICATIONS)
            // .permission(Permission.ACCESS_NOTIFICATION_POLICY)
            // .permission(Permission.BIND_NOTIFICATION_LISTENER_SERVICE)
            // 设置权限请求拦截器（局部设置）
            //.interceptor(new PermissionInterceptor())
            // 设置不触发错误检测机制（局部设置）
            //.unchecked()
            .request(new OnPermissionCallback() {

                @Override
                public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                }

                @Override
                public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                }
            });
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
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
