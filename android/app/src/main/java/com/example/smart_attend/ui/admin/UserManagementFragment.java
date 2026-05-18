package com.example.smart_attend.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smart_attend.databinding.FragmentUserManagementBinding;
import com.example.smart_attend.model.User;
import com.example.smart_attend.viewmodel.AdminViewModel;

public class UserManagementFragment extends Fragment {

    private FragmentUserManagementBinding binding;
    private AdminViewModel viewModel;
    private UserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        adapter = new UserAdapter(new UserAdapter.OnUserActionClickListener() {
            @Override
            public void onApproveTeacher(User user) {
                viewModel.updateUserRole(user.getId(), "teacher");
            }

            @Override
            public void onApproveStudent(User user) {
                viewModel.updateUserRole(user.getId(), "student");
            }
        });

        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewUsers.setAdapter(adapter);

        viewModel.getUsersList().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                adapter.setUsers(users);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch users when fragment is created
        viewModel.fetchUsers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
