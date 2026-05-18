package com.example.smart_attend.ui.student;

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
import com.example.smart_attend.network.RetrofitClient;
import com.example.smart_attend.utils.SessionManager;
import com.example.smart_attend.viewmodel.StudentAttendanceViewModel;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAttendanceFragment extends Fragment {

    private Spinner spinnerEnrolledSubject;
    private Button btnJoinSession;
    private TextView textNotEnrolled;
    private TextInputEditText editAttendanceCode;
    private List<Subject> enrolledSubjects = new ArrayList<>();
    private SessionManager sessionManager;
    private StudentAttendanceViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerEnrolledSubject = view.findViewById(R.id.spinnerEnrolledSubject);
        btnJoinSession = view.findViewById(R.id.btnJoinSession);
        textNotEnrolled = view.findViewById(R.id.textNotEnrolled);
        editAttendanceCode = view.findViewById(R.id.editAttendanceCode);

        sessionManager = new SessionManager(requireContext());
        
        viewModel = new ViewModelProvider(this).get(StudentAttendanceViewModel.class);
        viewModel.setAuthToken(sessionManager.getToken());

        setupObservers();
        loadEnrolledSubjects();

        btnJoinSession.setOnClickListener(v -> {
            if (enrolledSubjects.isEmpty()) {
                Toast.makeText(requireContext(), "Please enroll in a subject first", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String code = editAttendanceCode.getText() != null ? editAttendanceCode.getText().toString().trim() : "";
            if (code.length() != 4) {
                Toast.makeText(requireContext(), "Please enter a valid 4-letter code", Toast.LENGTH_SHORT).show();
                return;
            }

            int pos = spinnerEnrolledSubject.getSelectedItemPosition();
            if (pos >= 0 && pos < enrolledSubjects.size()) {
                Subject selected = enrolledSubjects.get(pos);
                viewModel.joinSession(selected.getId(), code);
            }
        });
    }

    private void setupObservers() {
        viewModel.getJoinSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), "Success! You are marked present.", Toast.LENGTH_LONG).show();
                editAttendanceCode.setText("");
                viewModel.resetJoinSuccess();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEnrolledSubjects() {
        String token = "Bearer " + sessionManager.getToken();
        RetrofitClient.getApiService().getEnrolledSubjects(token).enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    enrolledSubjects.clear();
                    enrolledSubjects.addAll(response.body());
                    updateSpinner();
                } else {
                    Toast.makeText(requireContext(), "Could not load your subjects", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSpinner() {
        if (enrolledSubjects.isEmpty()) {
            textNotEnrolled.setVisibility(View.VISIBLE);
            spinnerEnrolledSubject.setVisibility(View.GONE);
            btnJoinSession.setEnabled(false);
            editAttendanceCode.setEnabled(false);
        } else {
            textNotEnrolled.setVisibility(View.GONE);
            spinnerEnrolledSubject.setVisibility(View.VISIBLE);
            btnJoinSession.setEnabled(true);
            editAttendanceCode.setEnabled(true);

            ArrayAdapter<Subject> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    enrolledSubjects
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEnrolledSubject.setAdapter(adapter);
        }
    }
}
