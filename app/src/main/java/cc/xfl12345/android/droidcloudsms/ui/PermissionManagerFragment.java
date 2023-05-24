package cc.xfl12345.android.droidcloudsms.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentPermissionManagerBinding;
import cc.xfl12345.android.droidcloudsms.model.PermissionItem;

public class PermissionManagerFragment extends Fragment {

    private FragmentPermissionManagerBinding binding;

    private boolean needJumpBackWelcomePage = false;

    public PermissionManagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            needJumpBackWelcomePage = bundle.getBoolean("needJumpBackWelcomePage", false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_permission_manager, container, false);
        binding = FragmentPermissionManagerBinding.bind(view);

        MyApplication myApplication = ((MyApplication) requireContext().getApplicationContext());

        List<PermissionItem> permissionItemList = new ArrayList<>(MyApplication.permissionlist.size() + 2);

        permissionItemList.add(new PermissionItem() {
            @Override
            public String getDisplayName() {
                return "Shizuku";
            }

            @Override
            public String getCodeName() {
                return "Shizuku";
            }

            @Override
            public boolean isGranted() {
                return myApplication.getMyShizukuContext().isGranted();
            }

            @Override
            public void requestPermission(boolean beforeRequestStatus, boolean targetStatus) {
                getRequestPermissionCallback().callback(
                    beforeRequestStatus,
                    myApplication.getMyShizukuContext().requirePermission(),
                    targetStatus
                );
            }
        });
        MyApplication.permissionlist.forEach(entry -> permissionItemList.add(new PermissionItem() {
            @Override
            public String getDisplayName() {
                return entry.getValue();
            }

            @Override
            public String getCodeName() {
                return entry.getKey();
            }

            @Override
            public void requestPermission(boolean beforeRequestStatus, boolean targetStatus) {
                XXPermissions.with(requireContext())
                    .permission(getCodeName())
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                            getRequestPermissionCallback().callback(
                                beforeRequestStatus,
                                true,
                                targetStatus
                            );
                        }

                        @Override
                        public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                            getRequestPermissionCallback().callback(
                                beforeRequestStatus,
                                false,
                                targetStatus
                            );
                        }
                    });
            }

            @Override
            public boolean isGranted() {
                return XXPermissions.isGranted(requireContext(), getCodeName());
            }
        }));


        PermissionListAdapter adapter = new PermissionListAdapter(requireContext(), permissionItemList);
        if (needJumpBackWelcomePage) {
            adapter.setAfterButtonClickedListener((permissionName, granted) -> {
                boolean isAllGranted = true;
                for (PermissionItem item : permissionItemList) {
                    if (!item.isGranted()) {
                        isAllGranted = false;
                        break;
                    }
                }

                if (isAllGranted) {
                    try {
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                        navController.navigate(R.id.nav_welcome);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            });
        }
        binding.androidPermissionList.setAdapter(adapter);


        return view;
    }
}