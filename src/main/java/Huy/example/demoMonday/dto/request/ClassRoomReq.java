package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class ClassRoomReq {
    @NotNull
    private String name;
    @NotNull private java.util.UUID gradeLevelId;
    @NotNull private java.util.UUID schoolYearId;
    @NotNull private java.util.UUID schoolId;
    private java.util.UUID homeroomTeacherId;
    public String getName(){ return name; }
    public void setName(String v){ this.name=v; }
    public java.util.UUID getGradeLevelId(){ return gradeLevelId; }
    public void setGradeLevelId(java.util.UUID v){ this.gradeLevelId=v; }
    public java.util.UUID getSchoolYearId(){ return schoolYearId; }
    public void setSchoolYearId(java.util.UUID v){ this.schoolYearId=v; }
    public java.util.UUID getSchoolId(){ return schoolId; }
    public void setSchoolId(java.util.UUID v){ this.schoolId=v; }
    public java.util.UUID getHomeroomTeacherId(){ return homeroomTeacherId; }
    public void setHomeroomTeacherId(java.util.UUID v){ this.homeroomTeacherId=v; }
}
