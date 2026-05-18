package com.example.smart_attend.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_attend.R;
import com.example.smart_attend.databinding.ActivityLoginBinding;
import com.example.smart_attend.network.RetrofitClient;
import com.example.smart_attend.utils.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private GoogleSignInClient mGoogleSignInClient;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.googleSignInButton.setOnClickListener(v -> signIn());
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleSignInResult(task);
            }
    );

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String idToken = account.getIdToken();
                String requestedRole = getIntent().getStringExtra("REQUESTED_ROLE");
                sendTokenToServer(idToken, requestedRole != null ? requestedRole : "student");
            }
        } catch (ApiException e) {
            Log.w("LoginActivity", "Google sign in failed", e);
            Toast.makeText(this, "Sign in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            binding.loginProgressBar.setVisibility(View.GONE);
            binding.googleSignInButton.setEnabled(true);
        }
    }

    private void signIn() {
        binding.loginProgressBar.setVisibility(View.VISIBLE);
        binding.googleSignInButton.setEnabled(false);
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });
    }

    private void sendTokenToServer(String idToken, String requestedRole) {
        JsonObject payload = new JsonObject();
        payload.addProperty("id_token", idToken);
        payload.addProperty("requested_role", requestedRole);

        RetrofitClient.getApiService().authenticateWithGoogle(payload).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                binding.loginProgressBar.setVisibility(View.GONE);
                binding.googleSignInButton.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    String jwt = data.get("jwt_token").getAsString();
                    String role = data.get("role").getAsString();
                    
                    sessionManager.saveSession(jwt, role);
                    
                    Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                    
                    if ("pending".equals(role)) {
                        startActivity(new Intent(LoginActivity.this, PendingApprovalActivity.class));
                    } else if ("admin".equals(role)) {
                        startActivity(new Intent(LoginActivity.this, com.example.smart_attend.ui.admin.AdminDashboardActivity.class));
                    } else if ("teacher".equals(role)) {
                        startActivity(new Intent(LoginActivity.this, com.example.smart_attend.ui.teacher.TeacherDashboardActivity.class));
                    } else if ("student".equals(role)) {
                        startActivity(new Intent(LoginActivity.this, com.example.smart_attend.ui.student.StudentDashboardActivity.class));
                    }
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Authentication failed on server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                binding.loginProgressBar.setVisibility(View.GONE);
                binding.googleSignInButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
