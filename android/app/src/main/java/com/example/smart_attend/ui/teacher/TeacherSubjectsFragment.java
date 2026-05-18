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
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAddSubject;
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
        fabAddSubject = view.findViewById(R.id.fabAddSubject);

        fabAddSubject.setOnClickListener(v -> showAddSubjectDialog());

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
        adapter = new SubjectAdapter(false, subject -> {
            // Take Course action
            viewModel.claimSubject(subject.getId());
        });
        
        adapter.setDeleteListener(subject -> {
            // Remove Course action
            viewModel.deleteSubject(subject.getId());
        });
        
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
        
        viewModel.getSubjectClaimed().observe(getViewLifecycleOwner(), claimed -> {
            if (claimed != null && claimed) {
                Toast.makeText(requireContext(), "Course successfully claimed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddSubjectDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_subject, null);
        builder.setView(dialogView);
        builder.setTitle("Add New Subject");

        android.widget.EditText editName = dialogView.findViewById(R.id.editSubjectName);
        android.widget.EditText editCode = dialogView.findViewById(R.id.editSubjectCode);
        android.widget.EditText editCredits = dialogView.findViewById(R.id.editCreditHours);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = editName.getText().toString().trim();
            String code = editCode.getText().toString().trim();
            String creditsStr = editCredits.getText().toString().trim();

            if (name.isEmpty() || code.isEmpty() || creditsStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int credits = Integer.parseInt(creditsStr);
            viewModel.createSubject(name, code, credits);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
