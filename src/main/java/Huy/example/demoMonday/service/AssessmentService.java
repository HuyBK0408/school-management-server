package Huy.example.demoMonday.service;

import Huy.example.demoMonday.dto.request.AssessmentReq;
import Huy.example.demoMonday.dto.response.AssessmentResp;
import Huy.example.demoMonday.entity.Assessment;
import Huy.example.demoMonday.repo.AssessmentRepository;
import Huy.example.demoMonday.repo.ClassRoomRepository;
import Huy.example.demoMonday.repo.SubjectRepository;
import Huy.example.demoMonday.repo.TermRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AssessmentService {
    private final AssessmentRepository assessmentRepo;
    private final ClassRoomRepository classRepo;
    private final SubjectRepository subjectRepo;
    private final TermRepository termRepo;

    public AssessmentService(AssessmentRepository assessmentRepo, ClassRoomRepository classRepo,
                             SubjectRepository subjectRepo, TermRepository termRepo) {
        this.assessmentRepo = assessmentRepo;
        this.classRepo = classRepo;
        this.subjectRepo = subjectRepo;
        this.termRepo = termRepo;
    }

    private AssessmentResp toDto(Assessment a){
        return AssessmentResp.builder()
                .id(a.getId())
                .assessmentType(a.getAssessmentType())
                .weight(a.getWeight())
                .description(a.getDescription())
                .classId(a.getClassRoom().getId())
                .subjectId(a.getSubject().getId())
                .termId(a.getTerm().getId())
                .examDate(a.getExamDate())
                .build();
    }

    @Transactional
    public AssessmentResp create(AssessmentReq req){
        var a = new Assessment();
        a.setAssessmentType(req.getAssessmentType());
        a.setWeight(req.getWeight());
        a.setDescription(req.getDescription());
        a.setClassRoom(classRepo.findById(req.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found: " + req.getClassId())));
        a.setSubject(subjectRepo.findById(req.getSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + req.getSubjectId())));
        a.setTerm(termRepo.findById(req.getTermId())
                .orElseThrow(() -> new EntityNotFoundException("Term not found: " + req.getTermId())));
        a.setExamDate(req.getExamDate());
        return toDto(assessmentRepo.save(a));
    }

    @Transactional
    public AssessmentResp update(UUID id, AssessmentReq req){
        var a = assessmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found: " + id));
        a.setAssessmentType(req.getAssessmentType());
        a.setWeight(req.getWeight());
        a.setDescription(req.getDescription());
        if(!a.getClassRoom().getId().equals(req.getClassId()))
            a.setClassRoom(classRepo.findById(req.getClassId())
                    .orElseThrow(() -> new EntityNotFoundException("Class not found: " + req.getClassId())));
        if(!a.getSubject().getId().equals(req.getSubjectId()))
            a.setSubject(subjectRepo.findById(req.getSubjectId())
                    .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + req.getSubjectId())));
        if(!a.getTerm().getId().equals(req.getTermId()))
            a.setTerm(termRepo.findById(req.getTermId())
                    .orElseThrow(() -> new EntityNotFoundException("Term not found: " + req.getTermId())));
        a.setExamDate(req.getExamDate());
        return toDto(assessmentRepo.save(a));
    }

    public AssessmentResp get(UUID id){
        var a = assessmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found: " + id));
        return toDto(a);
    }

    public List<AssessmentResp> list(){
        return assessmentRepo.findAll().stream().map(this::toDto).toList();
    }

    public Page<AssessmentResp> page(Pageable pageable){
        return assessmentRepo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(UUID id){ assessmentRepo.deleteById(id); }
}

