package com.example.smart_attend.model;

public class Subject {
    private int id;
    private String name;
    private String code;
    private String teacher_name;
    private boolean is_enrolled;
    private boolean is_mine;
    private int credit_hours;

    public Subject() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTeacherName() { return teacher_name; }
    public void setTeacherName(String teacher_name) { this.teacher_name = teacher_name; }

    public boolean isEnrolled() { return is_enrolled; }
    public void setEnrolled(boolean enrolled) { is_enrolled = enrolled; }

    public boolean isMine() { return is_mine; }
    public void setMine(boolean mine) { is_mine = mine; }

    public int getCreditHours() { return credit_hours; }
    public void setCreditHours(int credit_hours) { this.credit_hours = credit_hours; }

    @Override
    public String toString() {
        // Used by the Spinner adapter to display subject names
        return name + " (" + code + ")";
    }
}
