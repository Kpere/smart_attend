package com.example.smart_attend.ui.teacher;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smart_attend.R;
import com.example.smart_attend.ui.adapters.SubjectAdapter;
import com.example.smart_attend.utils.SessionManager;
import com.example.smart_attend.viewmodel.TeacherViewModel;

public class TeacherSubjectsFragment extends Fragment {

    private RecyclerView rvSubjects;
    private ProgressBar progressBar;
    private TextView textEmpty;
    private SubjectAdapter adapter;
    private TeacherViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teacher_subjects, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvSubjects   = view.findViewById(R.id.rvSubjects);
        progressBar  = view.findViewById(R.id.progressBar);
        textEmpty    = view.findViewById(R.id.textEmpty);

        SessionManager sessionManager = new SessionManager(requireContext());

        // Shared ViewModel scoped to TeacherDashboardActivity
        viewModel = new ViewModelProvider(requireActivity()).get(TeacherViewModel.class);
        viewModel.setAuthToken(sessionManager.getToken());

        setupRecyclerView();
        observeData();

        // Load subjects
        viewModel.fetchSubjects();
    }

    private void setupRecyclerView() {
        // Pass false (teacher view), no delete listener → cards are read-only
        adapter = new SubjectAdapter(false, null);
        // No setDeleteListener() call → delete button stays hidden
        rvSubjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSubjects.setAdapter(adapter);
    }

    private void observeData() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getSubjectsList().observe(getViewLifecycleOwner(), subjects -> {
            if (subjects != null && !subjects.isEmpty()) {
                adapter.setSubjects(subjects);
                textEmpty.setVisibility(View.GONE);
                rvSubjects.setVisibility(View.VISIBLE);
            } else {
                textEmpty.setVisibility(View.VISIBLE);
                rvSubjects.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
