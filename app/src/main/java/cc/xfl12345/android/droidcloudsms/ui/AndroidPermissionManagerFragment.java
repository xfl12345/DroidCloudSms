package cc.xfl12345.android.droidcloudsms.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentAndroidPermissionManagerBinding;

public class AndroidPermissionManagerFragment extends Fragment {

    private FragmentAndroidPermissionManagerBinding binding;

    private ListView listView;

    private boolean needJumpBackWelcomePage = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            needJumpBackWelcomePage = bundle.getBoolean("needJumpBackWelcomePage", false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_android_permission_manager, container, false);
        binding = FragmentAndroidPermissionManagerBinding.bind(view);

        listView = binding.myList;
        AndroidPermissionListAdapter adapter = new AndroidPermissionListAdapter(requireContext(), MyApplication.permissionlist);
        if (needJumpBackWelcomePage) {
            adapter.setAfterButtonClickedListener((permissionName, granted) -> {
                if (((MyApplication) requireContext().getApplicationContext()).isAllPermissionGranted()) {
                    try {
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                        navController.navigate(R.id.nav_welcome);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            });
        }
        listView.setAdapter(adapter);
        return view;
    }
}
