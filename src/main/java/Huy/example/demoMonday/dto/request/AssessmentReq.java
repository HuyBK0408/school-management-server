package Huy.example.demoMonday.dto.request;

import Huy.example.demoMonday.enums.AssessmentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public class AssessmentReq {
    @NotNull
    private AssessmentType assessmentType;
    @NotNull @Min(1) private Integer weight;
    @Size(max=255) private String description;
    @NotNull private UUID classId;
    @NotNull private UUID subjectId;
    @NotNull private UUID termId;
    @NotNull private LocalDate examDate;

    public AssessmentType getAssessmentType(){ return assessmentType; }
    public void setAssessmentType(AssessmentType t){ this.assessmentType=t; }
    public Integer getWeight(){ return weight; }
    public void setWeight(Integer w){ this.weight=w; }
    public String getDescription(){ return description; }
    public void setDescription(String d){ this.description=d; }
    public UUID getClassId(){ return classId; }
    public void setClassId(UUID id){ this.classId=id; }
    public UUID getSubjectId(){ return subjectId; }
    public void setSubjectId(UUID id){ this.subjectId=id; }
    public UUID getTermId(){ return termId; }
    public void setTermId(UUID id){ this.termId=id; }
    public LocalDate getExamDate(){ return examDate; }
    public void setExamDate(LocalDate d){ this.examDate=d; }
}
