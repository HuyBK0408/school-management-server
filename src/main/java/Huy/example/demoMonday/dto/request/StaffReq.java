package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class StaffReq {
    @NotNull
    private String fullName;
    @NotNull private java.time.LocalDate dob;
    @NotNull private Huy.example.demoMonday.enums.Gender gender;
    @NotNull private String phone;
    @NotNull private String email;
    @NotNull private Huy.example.demoMonday.enums.StaffPosition position;
    @NotNull private java.util.UUID schoolId;
    private java.util.UUID userId;
    public String getFullName(){ return fullName; }
    public void setFullName(String v){ this.fullName=v; }
    public java.time.LocalDate getDob(){ return dob; }
    public void setDob(java.time.LocalDate v){ this.dob=v; }
    public Huy.example.demoMonday.enums.Gender getGender(){ return gender; }
    public void setGender(Huy.example.demoMonday.enums.Gender v){ this.gender=v; }
    public String getPhone(){ return phone; }
    public void setPhone(String v){ this.phone=v; }
    public String getEmail(){ return email; }
    public void setEmail(String v){ this.email=v; }
    public Huy.example.demoMonday.enums.StaffPosition getPosition(){ return position; }
    public void setPosition(Huy.example.demoMonday.enums.StaffPosition v){ this.position=v; }
    public java.util.UUID getSchoolId(){ return schoolId; }
    public void setSchoolId(java.util.UUID v){ this.schoolId=v; }
    public java.util.UUID getUserId(){ return userId; }
    public void setUserId(java.util.UUID v){ this.userId=v; }
}
