package cc.xfl12345.android.droidcloudsms.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentPermissionManagerBinding;
import cc.xfl12345.android.droidcloudsms.model.PermissionManager;
import cc.xfl12345.android.droidcloudsms.ui.adapter.PermissionListAdapter;

public class PermissionManagerFragment extends Fragment {

    private FragmentPermissionManagerBinding binding;

    private boolean needJumpBack = false;

    private PermissionManager permissionManager;

    public PermissionManagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            needJumpBack = bundle.getBoolean("needJumpBack", false);
        }

        permissionManager = ((MyApplication) requireContext().getApplicationContext()).getPermissionManager(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_permission_manager, container, false);
        binding = FragmentPermissionManagerBinding.bind(view);

        PermissionListAdapter adapter = new PermissionListAdapter(requireActivity(), permissionManager.getPermissionList());
        if (needJumpBack) {
            adapter.setAfterButtonClickedListener((permissionName, granted) -> {
                new Thread(() -> {
                    if (permissionManager.isAllPermissionGranted()) {
                        try {
                            requireActivity().runOnUiThread(() -> {
                                NavController navController = Navigation.findNavController(requireView());
                                navController.popBackStack();
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            });
        }

        binding.androidPermissionList.setAdapter(adapter);


        return view;
    }


}