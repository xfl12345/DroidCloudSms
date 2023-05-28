package cc.xfl12345.android.droidcloudsms.ui;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private NavController navController;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DrawerLayout drawerLayout = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // 为 安全退出APP的按钮 实现功能
        binding.navViewFooterBoxButtonExitApp.setOnClickListener((view) -> {
            new Thread(() -> {
                ((MyApplication) getApplication()).justExit();
            }).start();
        });

        mAppBarConfiguration = new AppBarConfiguration.Builder(
            R.id.nav_welcome,
            R.id.nav_permission_manager,
            R.id.nav_setting,
            R.id.nav_status,
            R.id.nav_test
        )
        .setOpenableLayout(drawerLayout)
        .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupWithNavController(navigationView, navController);

        FloatingActionButton floatingActionButton = binding.appBarMain.buttonNavigationPopup;
        floatingActionButton.setOnClickListener((view) -> binding.drawerLayout.open());


        // 如果未全部授权，则转跳至授权管理器界面
        new Thread(() -> {
            if (!((MyApplication) getApplication()).isAllPermissionGranted()) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("needJumpBack", true);
                drawerLayout.post(() -> navController.navigate(R.id.nav_permission_manager, bundle));
            }
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
