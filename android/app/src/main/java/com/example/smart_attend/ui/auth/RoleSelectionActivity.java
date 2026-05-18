package com.example.smart_attend.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smart_attend.databinding.ActivityRoleSelectionBinding;
import com.example.smart_attend.utils.SessionManager;
import com.example.smart_attend.ui.admin.AdminDashboardActivity;
import com.example.smart_attend.ui.teacher.TeacherDashboardActivity;
import com.example.smart_attend.ui.student.StudentDashboardActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    private ActivityRoleSelectionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoleSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check if user already has a saved valid session
        SessionManager sessionManager = new SessionManager(this);
        String savedToken = sessionManager.getToken();
        String savedRole  = sessionManager.getRole();

        if (savedToken != null && savedRole != null) {
            // User is already logged in — skip role selection and go straight to their dashboard
            routeToDashboard(savedRole);
            return; // Don't set up the role buttons — activity will finish
        }

        // No session — show the role selection UI as normal
        binding.btnRoleAdmin.setOnClickListener(v   -> navigateToLogin("admin"));
        binding.btnRoleTeacher.setOnClickListener(v -> navigateToLogin("teacher"));
        binding.btnRoleStudent.setOnClickListener(v -> navigateToLogin("student"));
    }

    private void navigateToLogin(String requestedRole) {
        Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
        intent.putExtra("REQUESTED_ROLE", requestedRole);
        startActivity(intent);
    }

    private void routeToDashboard(String role) {
        Intent intent;
        switch (role) {
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            case "teacher":
                intent = new Intent(this, TeacherDashboardActivity.class);
                break;
            case "student":
                intent = new Intent(this, StudentDashboardActivity.class);
                break;
            case "pending":
                intent = new Intent(this, PendingApprovalActivity.class);
                break;
            default:
                // Unknown role — force fresh login
                return;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
