package cc.xfl12345.android.droidcloudsms.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.databinding.FragmentLogBinding;
import cc.xfl12345.android.droidcloudsms.ui.adapter.SmsLogRecyclerAdapter;

public class SmsLogFragment extends Fragment {
    public SmsLogFragment() {
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

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setAdapter(new SmsLogRecyclerAdapter(requireContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));

        return view;
    }
}