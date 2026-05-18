package com.example.smart_attend.ui.teacher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.smart_attend.R;
import com.example.smart_attend.model.Subject;
import com.example.smart_attend.utils.SessionManager;
import com.example.smart_attend.viewmodel.TeacherViewModel;
import java.util.ArrayList;
import java.util.List;

public class TeacherAttendanceFragment extends Fragment {

    private Spinner spinnerSubject;
    private Button btnStartSession;
    private TextView textNoSubjects;
    private TeacherViewModel viewModel;
    private List<Subject> subjectList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teacher_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerSubject = view.findViewById(R.id.spinnerSubject);
        btnStartSession = view.findViewById(R.id.btnStartSession);
        textNoSubjects = view.findViewById(R.id.textNoSubjects);

        SessionManager sessionManager = new SessionManager(requireContext());

        // Share the same ViewModel as TeacherSubjectsFragment (scoped to Activity)
        viewModel = new ViewModelProvider(requireActivity()).get(TeacherViewModel.class);
        viewModel.setAuthToken(sessionManager.getToken());

        observeSubjects();

        // Fetch subjects if not already loaded
        if (viewModel.getSubjectsList().getValue() == null || viewModel.getSubjectsList().getValue().isEmpty()) {
            viewModel.fetchSubjects();
        }

        btnStartSession.setOnClickListener(v -> {
            if (subjectList.isEmpty()) {
                Toast.makeText(requireContext(), "Please add subjects first", Toast.LENGTH_SHORT).show();
                return;
            }
            int selectedPos = spinnerSubject.getSelectedItemPosition();
            if (selectedPos >= 0 && selectedPos < subjectList.size()) {
                Subject selected = subjectList.get(selectedPos);
                Toast.makeText(requireContext(),
                        "Session for \"" + selected.getName() + "\" — coming soon!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeSubjects() {
        viewModel.getSubjectsList().observe(getViewLifecycleOwner(), subjects -> {
            subjectList.clear();
            if (subjects != null) {
                subjectList.addAll(subjects);
            }
            updateSpinner();
        });
    }

    private void updateSpinner() {
        if (subjectList.isEmpty()) {
            textNoSubjects.setVisibility(View.VISIBLE);
            spinnerSubject.setVisibility(View.GONE);
            btnStartSession.setEnabled(false);
        } else {
            textNoSubjects.setVisibility(View.GONE);
            spinnerSubject.setVisibility(View.VISIBLE);
            btnStartSession.setEnabled(true);

            ArrayAdapter<Subject> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    subjectList
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSubject.setAdapter(adapter);
        }
    }
}
