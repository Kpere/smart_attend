package com.example.smart_attend.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.smart_attend.R;
import com.example.smart_attend.viewmodel.AdminViewModel;
import com.google.gson.JsonObject;

public class AdminOverviewFragment extends Fragment {

    private AdminViewModel viewModel;
    private ProgressBar progressBar;
    private TextView textTotalStudents, textTotalTeachers, textTotalSubjects, textPendingApprovals;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        textTotalStudents = view.findViewById(R.id.textTotalStudents);
        textTotalTeachers = view.findViewById(R.id.textTotalTeachers);
        textTotalSubjects = view.findViewById(R.id.textTotalSubjects);
        textPendingApprovals = view.findViewById(R.id.textPendingApprovals);

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        observeData();
        viewModel.fetchStats();
    }

    private void observeData() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                textTotalStudents.setText(String.valueOf(stats.get("total_students").getAsInt()));
                textTotalTeachers.setText(String.valueOf(stats.get("total_teachers").getAsInt()));
                textTotalSubjects.setText(String.valueOf(stats.get("total_subjects").getAsInt()));
                textPendingApprovals.setText(String.valueOf(stats.get("pending_approvals").getAsInt()));
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
