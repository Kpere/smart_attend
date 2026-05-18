package com.example.smart_attend.ui.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smart_attend.R;
import com.example.smart_attend.model.Subject;
import com.example.smart_attend.network.RetrofitClient;
import com.example.smart_attend.ui.adapters.SubjectAdapter;
import com.example.smart_attend.utils.SessionManager;
import com.google.gson.JsonObject;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentSubjectsFragment extends Fragment {

    private RecyclerView rvSubjects;
    private ProgressBar progressBar;
    private SubjectAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_subjects, container, false);
        
        rvSubjects = view.findViewById(R.id.rvSubjects);
        progressBar = view.findViewById(R.id.progressBar);
        
        sessionManager = new SessionManager(requireContext());
        
        setupRecyclerView();
        fetchSubjects();
        
        return view;
    }

    private void setupRecyclerView() {
        adapter = new SubjectAdapter(true, subject -> {
            enrollInSubject(subject);
        });
        rvSubjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSubjects.setAdapter(adapter);
    }

    private void fetchSubjects() {
        progressBar.setVisibility(View.VISIBLE);
        String token = "Bearer " + sessionManager.getToken();
        RetrofitClient.getApiService().getAllSubjects(token).enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setSubjects(response.body());
                } else {
                    Toast.makeText(requireContext(), "Failed to load subjects", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enrollInSubject(Subject subject) {
        String token = "Bearer " + sessionManager.getToken();
        JsonObject payload = new JsonObject();
        payload.addProperty("subject_id", subject.getId());

        RetrofitClient.getApiService().enrollSubject(token, payload).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Successfully enrolled in " + subject.getName(), Toast.LENGTH_SHORT).show();
                    fetchSubjects(); // Refresh the list to update button state
                } else {
                    Toast.makeText(requireContext(), "Failed to enroll", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
