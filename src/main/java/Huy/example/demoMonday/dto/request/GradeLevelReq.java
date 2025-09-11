package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class GradeLevelReq {
    @NotNull private String name;
    @NotNull
    private java.util.UUID schoolId;
    public String getName(){ return name; }
    public void setName(String v){ this.name=v; }
    public java.util.UUID getSchoolId(){ return schoolId; }
    public void setSchoolId(java.util.UUID v){ this.schoolId=v; }
}
