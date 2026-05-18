package com.example.smart_attend.ui.teacher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smart_attend.R;
import com.example.smart_attend.model.EnrolledStudent;
import com.example.smart_attend.model.Subject;
import com.example.smart_attend.network.RetrofitClient;
import com.example.smart_attend.ui.adapters.EnrolledStudentAdapter;
import com.example.smart_attend.utils.SessionManager;
import com.example.smart_attend.viewmodel.TeacherViewModel;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherStudentsFragment extends Fragment {

    private RecyclerView rvStudents;
    private ProgressBar progressBar;
    private TextView textEmpty;
    private TextView textStudentCount;
    private Spinner spinnerSubjectFilter;
    private EnrolledStudentAdapter adapter;
    private TeacherViewModel viewModel;
    private SessionManager sessionManager;

    private List<EnrolledStudent> allStudents = new ArrayList<>();
    private List<Subject> subjects = new ArrayList<>();
    // Index 0 = "All Subjects", indices 1..N map to subjects list
    private int selectedSubjectIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teacher_students, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvStudents = view.findViewById(R.id.rvStudents);
        progressBar = view.findViewById(R.id.progressBar);
        textEmpty = view.findViewById(R.id.textEmpty);
        textStudentCount = view.findViewById(R.id.textStudentCount);
        spinnerSubjectFilter = view.findViewById(R.id.spinnerSubjectFilter);

        sessionManager = new SessionManager(requireContext());

        // Share ViewModel with the Subjects tab
        viewModel = new ViewModelProvider(requireActivity()).get(TeacherViewModel.class);
        viewModel.setAuthToken(sessionManager.getToken());

        setupRecyclerView();
        observeSubjects();
        fetchStudents();

        // Fetch subjects if not yet loaded
        if (viewModel.getSubjectsList().getValue() == null || viewModel.getSubjectsList().getValue().isEmpty()) {
            viewModel.fetchSubjects();
        }
    }

    private void setupRecyclerView() {
        adapter = new EnrolledStudentAdapter();
        rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStudents.setAdapter(adapter);
    }

    private void observeSubjects() {
        viewModel.getSubjectsList().observe(getViewLifecycleOwner(), subjectList -> {
            subjects.clear();
            if (subjectList != null) subjects.addAll(subjectList);
            buildSubjectSpinner();
        });
    }

    private void buildSubjectSpinner() {
        List<String> options = new ArrayList<>();
        options.add("All Subjects");
        for (Subject s : subjects) {
            options.add(s.getName() + " (" + s.getCode() + ")");
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                options
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubjectFilter.setAdapter(spinnerAdapter);

        spinnerSubjectFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSubjectIndex = position;
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchStudents() {
        progressBar.setVisibility(View.VISIBLE);
        String token = "Bearer " + sessionManager.getToken();

        RetrofitClient.getApiService().getTeacherStudents(token).enqueue(new Callback<List<EnrolledStudent>>() {
            @Override
            public void onResponse(Call<List<EnrolledStudent>> call, Response<List<EnrolledStudent>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allStudents.clear();
                    allStudents.addAll(response.body());
                    applyFilter();
                } else {
                    Toast.makeText(requireContext(), "Failed to load students", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EnrolledStudent>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter() {
        List<EnrolledStudent> filtered = new ArrayList<>();

        if (selectedSubjectIndex == 0) {
            // "All Subjects"
            filtered.addAll(allStudents);
        } else {
            // Filter by the selected subject (index offset by 1 because "All" is index 0)
            Subject selected = subjects.get(selectedSubjectIndex - 1);
            for (EnrolledStudent s : allStudents) {
                if (s.getSubjectId() == selected.getId()) {
                    filtered.add(s);
                }
            }
        }

        adapter.setStudents(filtered);

        String countLabel = filtered.size() + " student" + (filtered.size() != 1 ? "s" : "");
        textStudentCount.setText(countLabel);

        textEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        rvStudents.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
