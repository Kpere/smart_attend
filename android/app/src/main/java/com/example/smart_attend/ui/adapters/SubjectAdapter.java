package com.example.smart_attend.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smart_attend.R;
import com.example.smart_attend.model.Subject;
import java.util.ArrayList;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<Subject> subjectList = new ArrayList<>();
    private OnSubjectActionClickListener actionListener;
    private OnSubjectDeleteClickListener deleteListener;
    private boolean isStudentView;

    public interface OnSubjectActionClickListener {
        void onActionClick(Subject subject);
    }

    public interface OnSubjectDeleteClickListener {
        void onDeleteClick(Subject subject);
    }

    public SubjectAdapter(boolean isStudentView, OnSubjectActionClickListener actionListener) {
        this.isStudentView = isStudentView;
        this.actionListener = actionListener;
    }

    public void setDeleteListener(OnSubjectDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjectList = subjects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.textSubjectName.setText(subject.getName());
        holder.textSubjectCode.setText(subject.getCode());
        holder.textCreditHours.setText("Credits: " + subject.getCreditHours());

        if (isStudentView) {
            holder.textTeacherName.setVisibility(View.VISIBLE);
            holder.textTeacherName.setText("Teacher: " + subject.getTeacherName());
            holder.btnDelete.setVisibility(View.GONE);

            holder.btnAction.setVisibility(View.VISIBLE);
            if (subject.isEnrolled()) {
                holder.btnAction.setText("Enrolled");
                holder.btnAction.setEnabled(false);
            } else {
                holder.btnAction.setText("Register");
                holder.btnAction.setEnabled(true);
                holder.btnAction.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onActionClick(subject);
                });
            }
        } else {
            // Teacher view — show teacher name, no action buttons
            holder.textTeacherName.setVisibility(View.VISIBLE);
            holder.textTeacherName.setText("Teacher: " + subject.getTeacherName());
            holder.btnAction.setVisibility(View.GONE);

            // Only show delete button if a delete listener is explicitly set
            if (deleteListener != null) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(subject));
            } else {
                holder.btnDelete.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView textSubjectName, textTeacherName, textSubjectCode, textCreditHours;
        Button btnAction;
        ImageButton btnDelete;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            textSubjectName = itemView.findViewById(R.id.textSubjectName);
            textTeacherName = itemView.findViewById(R.id.textTeacherName);
            textSubjectCode = itemView.findViewById(R.id.textSubjectCode);
            textCreditHours = itemView.findViewById(R.id.textCreditHours);
            btnAction = itemView.findViewById(R.id.btnAction);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
