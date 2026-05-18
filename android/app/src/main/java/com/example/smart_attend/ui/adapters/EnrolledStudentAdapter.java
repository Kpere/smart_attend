package com.example.smart_attend.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smart_attend.R;
import com.example.smart_attend.model.EnrolledStudent;
import java.util.ArrayList;
import java.util.List;

public class EnrolledStudentAdapter extends RecyclerView.Adapter<EnrolledStudentAdapter.StudentViewHolder> {

    private List<EnrolledStudent> studentList = new ArrayList<>();

    public void setStudents(List<EnrolledStudent> students) {
        this.studentList = students;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_enrolled_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        EnrolledStudent student = studentList.get(position);
        holder.textStudentName.setText(student.getStudentName());
        holder.textStudentEmail.setText(student.getStudentEmail());
        holder.textSubjectTag.setText(student.getSubjectName());
        
        int percent = student.getAttendancePercentage();
        holder.textAttendancePercentage.setText(percent + "% Attended");
        
        if (percent >= 75) {
            holder.textAttendancePercentage.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Green
        } else if (percent >= 50) {
            holder.textAttendancePercentage.setTextColor(android.graphics.Color.parseColor("#FF9800")); // Orange
        } else {
            holder.textAttendancePercentage.setTextColor(android.graphics.Color.parseColor("#F44336")); // Red
        }
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView textStudentName, textStudentEmail, textSubjectTag, textAttendancePercentage;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            textStudentName = itemView.findViewById(R.id.textStudentName);
            textStudentEmail = itemView.findViewById(R.id.textStudentEmail);
            textSubjectTag = itemView.findViewById(R.id.textSubjectTag);
            textAttendancePercentage = itemView.findViewById(R.id.textAttendancePercentage);
        }
    }
}
