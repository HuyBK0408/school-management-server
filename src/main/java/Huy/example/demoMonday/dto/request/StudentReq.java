package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class StudentReq {
    @NotNull
    private String fullName;
    @NotNull private java.time.LocalDate dob;
    @NotNull private Huy.example.demoMonday.enums.Gender gender;
    @NotNull private String studentCode;
    private java.util.UUID currentClassId;
    @NotNull private java.util.UUID schoolId;
    private String photoUrl;
    @NotNull private Huy.example.demoMonday.enums.StudentStatus status;
    public String getFullName(){ return fullName; }
    public void setFullName(String v){ this.fullName=v; }
    public java.time.LocalDate getDob(){ return dob; }
    public void setDob(java.time.LocalDate v){ this.dob=v; }
    public Huy.example.demoMonday.enums.Gender getGender(){ return gender; }
    public void setGender(Huy.example.demoMonday.enums.Gender v){ this.gender=v; }
    public String getStudentCode(){ return studentCode; }
    public void setStudentCode(String v){ this.studentCode=v; }
    public java.util.UUID getCurrentClassId(){ return currentClassId; }
    public void setCurrentClassId(java.util.UUID v){ this.currentClassId=v; }
    public java.util.UUID getSchoolId(){ return schoolId; }
    public void setSchoolId(java.util.UUID v){ this.schoolId=v; }
    public String getPhotoUrl(){ return photoUrl; }
    public void setPhotoUrl(String v){ this.photoUrl=v; }
    public Huy.example.demoMonday.enums.StudentStatus getStatus(){ return status; }
    public void setStatus(Huy.example.demoMonday.enums.StudentStatus v){ this.status=v; }
}
