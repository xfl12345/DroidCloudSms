package cc.xfl12345.android.droidcloudsms.ui;

import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentLogBinding;

public class LogFragment extends Fragment {
    public LogFragment() {
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
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        FragmentLogBinding binding = FragmentLogBinding.bind(view);

        binding.contentBox.setText(getString(R.string.super_huge_string));
        binding.contentExpandButton.setOnClickListener(v -> binding.contentBox.toggle());

        // binding.contentBox.setContent(getString(R.string.super_huge_string));
        // binding.contentBox.getWidth();
        // ExpandableTextView expandableTextView = binding.contentBox;
        // expandableTextView.setEndExpendContent("...");
        // expandableTextView.setContent(getString(R.string.super_huge_string));


        return view;
    }
}