package com.labmatch.model;

public class Student {

    private String rollNo;
    private String name;
    private String branch;

    public Student(
            String rollNo,
            String name,
            String branch
    ) {
        this.rollNo = rollNo;
        this.name = name;
        this.branch = branch;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Override
    public String toString() {
        return "Student{" +
                "rollNo='" + rollNo + '\'' +
                ", name='" + name + '\'' +
                ", branch='" + branch + '\'' +
                '}';
    }
}