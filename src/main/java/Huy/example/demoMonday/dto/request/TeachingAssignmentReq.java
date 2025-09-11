package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class TeachingAssignmentReq {
    @NotNull
    private java.util.UUID teacherId;
    @NotNull private java.util.UUID classId;
    @NotNull private java.util.UUID subjectId;
    @NotNull private java.util.UUID schoolYearId;
    @NotNull private java.time.LocalDate assignedFrom;
    @NotNull private java.time.LocalDate assignedTo;
    public java.util.UUID getTeacherId(){ return teacherId; }
    public void setTeacherId(java.util.UUID v){ this.teacherId=v; }
    public java.util.UUID getClassId(){ return classId; }
    public void setClassId(java.util.UUID v){ this.classId=v; }
    public java.util.UUID getSubjectId(){ return subjectId; }
    public void setSubjectId(java.util.UUID v){ this.subjectId=v; }
    public java.util.UUID getSchoolYearId(){ return schoolYearId; }
    public void setSchoolYearId(java.util.UUID v){ this.schoolYearId=v; }
    public java.time.LocalDate getAssignedFrom(){ return assignedFrom; }
    public void setAssignedFrom(java.time.LocalDate v){ this.assignedFrom=v; }
    public java.time.LocalDate getAssignedTo(){ return assignedTo; }
    public void setAssignedTo(java.time.LocalDate v){ this.assignedTo=v; }
}
