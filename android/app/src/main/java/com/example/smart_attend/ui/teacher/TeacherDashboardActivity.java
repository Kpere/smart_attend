package com.example.smart_attend.ui.teacher;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smart_attend.R;

public class TeacherDashboardActivity extends AppCompatActivity {
    private com.example.smart_attend.databinding.ActivityTeacherDashboardBinding binding;
    private com.example.smart_attend.utils.SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.example.smart_attend.databinding.ActivityTeacherDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setSupportActionBar(binding.toolbar);
        sessionManager = new com.example.smart_attend.utils.SessionManager(this);

        androidx.navigation.fragment.NavHostFragment navHostFragment = (androidx.navigation.fragment.NavHostFragment) getSupportFragmentManager().findFragmentById(com.example.smart_attend.R.id.nav_host_fragment);
        if (navHostFragment != null) {
            androidx.navigation.NavController navController = navHostFragment.getNavController();
            androidx.navigation.ui.NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
            
            // Update title based on fragment
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(destination.getLabel());
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        com.example.smart_attend.utils.SessionManager sessionManager = new com.example.smart_attend.utils.SessionManager(this);
        sessionManager.clearSession();

        com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        com.google.android.gms.auth.api.signin.GoogleSignInClient mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            android.content.Intent intent = new android.content.Intent(this, com.example.smart_attend.ui.auth.RoleSelectionActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
