package com.example.smart_attend.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smart_attend.databinding.ItemUserBinding;
import com.example.smart_attend.model.User;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users = new ArrayList<>();
    private final OnUserActionClickListener listener;

    public interface OnUserActionClickListener {
        void onApproveTeacher(User user);
        void onApproveStudent(User user);
    }

    public UserAdapter(OnUserActionClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserBinding binding;

        public UserViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user) {
            binding.textName.setText(user.getName());
            binding.textEmail.setText(user.getEmail());
            binding.textRole.setText(user.getRole());

            if ("pending".equals(user.getRole())) {
                binding.btnApproveTeacher.setVisibility(View.VISIBLE);
                binding.btnApproveStudent.setVisibility(View.VISIBLE);
            } else {
                binding.btnApproveTeacher.setVisibility(View.GONE);
                binding.btnApproveStudent.setVisibility(View.GONE);
            }

            binding.btnApproveTeacher.setOnClickListener(v -> {
                if (listener != null) listener.onApproveTeacher(user);
            });

            binding.btnApproveStudent.setOnClickListener(v -> {
                if (listener != null) listener.onApproveStudent(user);
            });
        }
    }
}
