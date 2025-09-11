package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class SchoolYearReq {
    @NotNull
    private String code;
    @NotNull private java.time.LocalDate startDate;
    @NotNull private java.time.LocalDate endDate;
    @NotNull private java.util.UUID schoolId;
    public String getCode(){ return code; }
    public void setCode(String v){ this.code=v; }
    public java.time.LocalDate getStartDate(){ return startDate; }
    public void setStartDate(java.time.LocalDate v){ this.startDate=v; }
    public java.time.LocalDate getEndDate(){ return endDate; }
    public void setEndDate(java.time.LocalDate v){ this.endDate=v; }
    public java.util.UUID getSchoolId(){ return schoolId; }
    public void setSchoolId(java.util.UUID v){ this.schoolId=v; }
}