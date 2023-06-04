package cc.xfl12345.android.droidcloudsms.ui;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.WebsocketService;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentHomeBinding;
import cc.xfl12345.android.droidcloudsms.model.OvalTouchAreaFilter;
import cc.xfl12345.android.droidcloudsms.model.WebSocketServiceConnectionListener;
import cc.xfl12345.android.droidcloudsms.model.ws.WebSocketManager;
import okhttp3.Response;

public class HomeFragment extends Fragment implements WebSocketServiceConnectionListener {

    private MyApplication context;

    private FragmentHomeBinding binding;

    private WebsocketService websocketService = null;

    private WebSocketManager.StatusListener webSocketStatusListener = null;

    private ButtonStatus buttonStatus = ButtonStatus.PENDING;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (MyApplication) requireContext().getApplicationContext();
        webSocketStatusListener = new WebSocketManager.StatusListener() {
            @Override
            public void onConnected() {
                HomeFragment.this.updateButton(isAllOK() ? ButtonStatus.OK : ButtonStatus.FAILED, "");
            }

            @Override
            public void onDisconnected(@Nullable String reason, @Nullable Integer code, @Nullable Throwable throwable, @Nullable Response response) {
                if (websocketService != null && websocketService.isRecreatingWebSocket()) {
                    return;
                }
                HomeFragment.this.updateButton(isAllOK() ? ButtonStatus.OK : ButtonStatus.FAILED, "");
            }

            @Override
            public void onRetryMaxReached() {
                HomeFragment.this.updateButton(isAllOK() ? ButtonStatus.OK : ButtonStatus.FAILED, "");
            }

            @Override
            public void onReconnecting() {
                HomeFragment.this.updateButton(ButtonStatus.PENDING, "WebSocket 正在重连");
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        binding = FragmentHomeBinding.bind(view);

        // binding.testButton.setTouchDelegate(new TouchDelegate());
        // binding.testButton.setOnTouchListener(new View.OnTouchListener() {
        //     @Override
        //     public boolean onTouch(View v, MotionEvent event) {
        //         float radius = v.getWidth() / 2f;
        //         float x = event.getX() - radius;
        //         float y = event.getY() - radius;
        //         boolean flag = isInCircle(radius, x, y);
        //         Log.d("点击测试", String.format("X=%s,Y=%s, IsClick=%s", event.getX(), event.getY(), flag));
        //         if (flag) {
        //             if (event.getAction() == MotionEvent.ACTION_DOWN) {
        //                 // perform your action
        //             }
        //             return true;
        //         } else {
        //             return false;
        //         }
        //     }
        //
        //     private boolean isInCircle(float radius, float x, float y) {
        //         if (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) < radius) {
        //             //touch was performed inside the circle;
        //             return true;
        //         } else {
        //             //touched ouside the circle
        //             return false;
        //         }
        //     }
        // });
        // binding.testButton.setOnClickListener((v) -> {
        //     Log.d("点击测试", "TestButton 已点击");
        // });


        MaterialButton button = binding.fragmentHomeButton;
        // binding.ttttt.setOnTouchListener((v, event) -> {
        //     Log.d("点击测试", "爸爸知道了。" + String.format("X=%s,Y=%s, IsClick=%s", event.getX(), event.getY(), true));
        //     Log.d("点击测试", "原始绝对坐标：" + String.format("X=%s,Y=%s", event.getRawX(), event.getRawY()));
        //     return false;
        // });

        button.setOnTouchListener(new OvalTouchAreaFilter());
        button.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    updateButton(ButtonStatus.PENDING, "正在重新初始化WebSocket");
                    if (websocketService != null) {
                        websocketService.initWebSocket();
                    } else {
                        updateButton(ButtonStatus.FAILED, "");
                    }
                } catch (Exception e) {
                    // ignore
                }
            }, HomeFragment.class.getName() + "_button_click").start();
        });

        updateButton(ButtonStatus.PENDING, "正在初始化UI");

        return view;
    }

    public boolean isAllOK() {
        return websocketService != null && websocketService.isSmsReady() && websocketService.isWebSocketConnected();
    }

    public void updateButton(ButtonStatus status, String pendingMessage) {
        HomeFragment.this.buttonStatus = status;
        MaterialButton button = binding.fragmentHomeButton;
        button.post(() -> {
            switch (status) {
                case PENDING:
                    button.setBackgroundColor(Color.YELLOW);
                    button.setIcon(AppCompatResources.getDrawable(context, R.drawable.baseline_360_24));
                    button.setText(pendingMessage);
                    break;
                case OK:
                    button.setBackgroundColor(Color.GREEN);
                    button.setIcon(AppCompatResources.getDrawable(
                        context,
                        R.drawable.baseline_check_24
                    ));
                    button.setText("工作正常");
                    break;
                case FAILED:
                    button.setBackgroundColor(Color.RED);
                    button.setIcon(AppCompatResources.getDrawable(
                        context,
                        R.drawable.baseline_close_24
                    ));
                    button.setText("未工作");
                    break;
            }
        });
    }


    @Override
    public void onResume() {
        context.addWebSocketServiceConnectionListener(this);
        super.onResume();
        if (websocketService == null || ButtonStatus.PENDING.equals(buttonStatus)) {
            updateButton(isAllOK() ? ButtonStatus.OK : ButtonStatus.FAILED, "");
        }
    }

    @Override
    public void onPause() {
        if (websocketService != null) {
            websocketService.removeListener(webSocketStatusListener);
        }
        context.removeWebSocketServiceConnectionListener(this);
        super.onPause();
    }

    @Override
    public void onServiceConnected(WebsocketService service) {
        websocketService = service;
        websocketService.addListener(webSocketStatusListener);
    }

    @Override
    public void onServiceDisconnected() {
        websocketService = null;
    }

    enum ButtonStatus {
        PENDING,
        OK,
        FAILED
    }
}
