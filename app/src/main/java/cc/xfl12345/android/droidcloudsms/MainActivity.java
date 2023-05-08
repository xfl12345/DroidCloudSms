package cc.xfl12345.android.droidcloudsms;

import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import java.util.List;

import cc.xfl12345.android.droidcloudsms.databinding.ActivityMainBinding;
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
                if (myShizukuContext.requirePermission()) {
                    for (int i = 0; i < 10; i++) {
                        Log.i("cc.xfl12345.android.xposed.mysmssender", "Current UID=" + Shizuku.getUid());
                    }

                    myShizukuContext.initService();
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}