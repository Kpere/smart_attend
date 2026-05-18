package com.example.smart_attend.ui.teacher;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.example.smart_attend.viewmodel.TeacherAttendanceViewModel;
import com.example.smart_attend.viewmodel.TeacherViewModel;
import java.util.ArrayList;
import java.util.List;

public class TeacherAttendanceFragment extends Fragment {

    private Spinner spinnerSubject;
    private Button btnStartSession;
    private TextView textNoSubjects, textSessionCode, textTimer;
    private LinearLayout layoutActiveSession;
    private TeacherViewModel subjectViewModel;
    private TeacherAttendanceViewModel attendanceViewModel;
    private List<Subject> mySubjects = new ArrayList<>();
    private CountDownTimer countDownTimer;

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
        textSessionCode = view.findViewById(R.id.textSessionCode);
        textTimer = view.findViewById(R.id.textTimer);
        layoutActiveSession = view.findViewById(R.id.layoutActiveSession);

        SessionManager sessionManager = new SessionManager(requireContext());
        String token = sessionManager.getToken();

        subjectViewModel = new ViewModelProvider(requireActivity()).get(TeacherViewModel.class);
        subjectViewModel.setAuthToken(token);

        attendanceViewModel = new ViewModelProvider(this).get(TeacherAttendanceViewModel.class);
        attendanceViewModel.setAuthToken(token);

        setupObservers();

        // Fetch subjects if not already loaded
        if (subjectViewModel.getSubjectsList().getValue() == null || subjectViewModel.getSubjectsList().getValue().isEmpty()) {
            subjectViewModel.fetchSubjects();
        }

        btnStartSession.setOnClickListener(v -> {
            int selectedPos = spinnerSubject.getSelectedItemPosition();
            if (selectedPos >= 0 && selectedPos < mySubjects.size()) {
                Subject selected = mySubjects.get(selectedPos);
                attendanceViewModel.startSession(selected.getId());
            }
        });
    }

    private void setupObservers() {
        subjectViewModel.getSubjectsList().observe(getViewLifecycleOwner(), subjects -> {
            mySubjects.clear();
            if (subjects != null) {
                for (Subject s : subjects) {
                    if (s.isMine()) mySubjects.add(s);
                }
            }
            if (mySubjects.isEmpty()) {
                spinnerSubject.setVisibility(View.GONE);
                btnStartSession.setVisibility(View.GONE);
                textNoSubjects.setVisibility(View.VISIBLE);
            } else {
                spinnerSubject.setVisibility(View.VISIBLE);
                btnStartSession.setVisibility(View.VISIBLE);
                textNoSubjects.setVisibility(View.GONE);
                ArrayAdapter<Subject> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, mySubjects);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSubject.setAdapter(adapter);
            }
        });

        attendanceViewModel.getSessionCode().observe(getViewLifecycleOwner(), code -> {
            if (code != null) {
                // Session started successfully
                btnStartSession.setVisibility(View.GONE);
                spinnerSubject.setEnabled(false);
                layoutActiveSession.setVisibility(View.VISIBLE);
                textSessionCode.setText(code);
                startCountdownTimer();
            } else {
                // Session ended or cleared
                btnStartSession.setVisibility(View.VISIBLE);
                spinnerSubject.setEnabled(true);
                layoutActiveSession.setVisibility(View.GONE);
            }
        });

        attendanceViewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCountdownTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        
        // 10 minutes = 600,000 ms
        countDownTimer = new CountDownTimer(600000, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                textTimer.setText(String.format("%02d:%02d", minutes, seconds));
            }

            public void onFinish() {
                textTimer.setText("00:00");
                Toast.makeText(requireContext(), "Session has expired", Toast.LENGTH_LONG).show();
                attendanceViewModel.clearSessionCode();
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
