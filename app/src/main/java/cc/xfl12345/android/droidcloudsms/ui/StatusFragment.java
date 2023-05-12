package cc.xfl12345.android.droidcloudsms.ui;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentStatusBinding;

public class StatusFragment extends Fragment {

    public StatusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        FragmentStatusBinding binding = FragmentStatusBinding.bind(view);

        boolean shizukuGranted = ((MyApplication) requireContext().getApplicationContext()).getMyShizukuContext().isGranted();
        binding.shizukuStatusMonitor.setText(shizukuGranted ? "已授权" : "未授权");
        binding.shizukuStatusMonitor.setTextColor(shizukuGranted ? Color.GREEN : Color.RED);

        return view;
    }
}