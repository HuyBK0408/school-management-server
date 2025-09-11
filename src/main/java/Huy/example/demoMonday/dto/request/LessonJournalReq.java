package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class LessonJournalReq {
    @NotNull
    private java.util.UUID classId;
    @NotNull private java.util.UUID subjectId;
    @NotNull private java.util.UUID teacherId;
    @NotNull private java.time.LocalDate teachDate;
    @NotNull private Integer periodNo;
    @NotNull private String content;
    private java.util.UUID transferredFromTeacherId;
    public java.util.UUID getClassId(){ return classId; }
    public void setClassId(java.util.UUID v){ this.classId=v; }
    public java.util.UUID getSubjectId(){ return subjectId; }
    public void setSubjectId(java.util.UUID v){ this.subjectId=v; }
    public java.util.UUID getTeacherId(){ return teacherId; }
    public void setTeacherId(java.util.UUID v){ this.teacherId=v; }
    public java.time.LocalDate getTeachDate(){ return teachDate; }
    public void setTeachDate(java.time.LocalDate v){ this.teachDate=v; }
    public Integer getPeriodNo(){ return periodNo; }
    public void setPeriodNo(Integer v){ this.periodNo=v; }
    public String getContent(){ return content; }
    public void setContent(String v){ this.content=v; }
    public java.util.UUID getTransferredFromTeacherId(){ return transferredFromTeacherId; }
    public void setTransferredFromTeacherId(java.util.UUID v){ this.transferredFromTeacherId=v; }
}
