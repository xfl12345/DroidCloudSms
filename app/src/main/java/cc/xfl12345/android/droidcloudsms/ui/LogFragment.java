package cc.xfl12345.android.droidcloudsms.ui;

import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


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

        at.blogc.android.views.ExpandableTextView contentBox = binding.contentBox;
        Button contentExpandButton = binding.contentExpandButton;

        contentBox.setText(R.string.super_huge_string);
        contentExpandButton.setText(R.string.click_me_to_expand);
        contentExpandButton.setOnClickListener(v -> {
            // 如果已经展开，点击之后会收起，所以要显示“点我展开”
            contentExpandButton.setText(contentBox.isExpanded()
                ? R.string.click_me_to_expand
                : R.string.click_me_to_collapse
            );
            contentBox.toggle();
        });


        return view;
    }
}