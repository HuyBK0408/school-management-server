package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class SubjectReq {
    @NotNull
    private String name;
    @NotNull private String code;
    @NotNull private java.util.UUID schoolId;
    public String getName(){ return name; }
    public void setName(String v){ this.name=v; }
    public String getCode(){ return code; }
    public void setCode(String v){ this.code=v; }
    public java.util.UUID getSchoolId(){ return schoolId; }
    public void setSchoolId(java.util.UUID v){ this.schoolId=v; }
}
