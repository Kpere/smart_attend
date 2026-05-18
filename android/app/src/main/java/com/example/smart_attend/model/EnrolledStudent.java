package com.example.smart_attend.model;

import com.google.gson.annotations.SerializedName;

public class EnrolledStudent {
    private int student_id;
    private String student_name;
    private String student_email;
    private int subject_id;
    private String subject_name;
    @SerializedName("enrollment_status")
    private String enrollment_status;

    @SerializedName("attendance_percentage")
    private int attendancePercentage;

    public EnrolledStudent() {}

    public int getStudentId() { return student_id; }
    public String getStudentName() { return student_name; }
    public String getStudentEmail() { return student_email; }
    public int getSubjectId() { return subject_id; }
    public String getSubjectName() { return subject_name; }
    public String getEnrollmentStatus() { return enrollment_status; }
    public int getAttendancePercentage() { return attendancePercentage; }
}
