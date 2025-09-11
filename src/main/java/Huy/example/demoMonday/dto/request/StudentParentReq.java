package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class StudentParentReq {
    @NotNull
    private java.util.UUID studentId;
    @NotNull private java.util.UUID parentId;
    public java.util.UUID getStudentId(){ return studentId; }
    public void setStudentId(java.util.UUID v){ this.studentId=v; }
    public java.util.UUID getParentId(){ return parentId; }
    public void setParentId(java.util.UUID v){ this.parentId=v; }
}
