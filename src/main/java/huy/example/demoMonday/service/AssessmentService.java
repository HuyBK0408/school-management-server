package huy.example.demoMonday.service;

import huy.example.demoMonday.dto.request.AssessmentReq;
import huy.example.demoMonday.dto.response.AssessmentResp;
import huy.example.demoMonday.entity.Assessment;
import huy.example.demoMonday.enums.AssessmentType;
import huy.example.demoMonday.repo.AssessmentRepository;
import huy.example.demoMonday.repo.ClassRoomRepository;
import huy.example.demoMonday.repo.SubjectRepository;
import huy.example.demoMonday.repo.TermRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager em;

    public AssessmentService(AssessmentRepository assessmentRepo,
                             ClassRoomRepository classRepo,
                             SubjectRepository subjectRepo,
                             TermRepository termRepo) {
        this.assessmentRepo = assessmentRepo;
        this.classRepo = classRepo;
        this.subjectRepo = subjectRepo;
        this.termRepo = termRepo;
    }

    private AssessmentResp toDto(Assessment a){
        return AssessmentResp.builder()
                .id(a.getId())
                .assessmentType(a.getType())
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
        var cls  = classRepo.findById(req.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found: " + req.getClassId()));
        var subj = subjectRepo.findById(req.getSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + req.getSubjectId()));
        var term = termRepo.findById(req.getTermId())
                .orElseThrow(() -> new EntityNotFoundException("Term not found: " + req.getTermId()));

        // business rules
        switch (req.getAssessmentType()) {
            case ASSIGNMENT -> {
                long cnt = assessmentRepo.countByClassRoom_IdAndSubject_IdAndTerm_IdAndType(
                        cls.getId(), subj.getId(), term.getId(), AssessmentType.ASSIGNMENT);
                if (cnt >= 2) throw new IllegalStateException(
                        "At most 2 ASSIGNMENT per (class, subject, term)");
            }
            case MIDTERM, FINAL -> {
                boolean exists = assessmentRepo.existsByClassRoom_IdAndSubject_IdAndTerm_IdAndType(
                        cls.getId(), subj.getId(), term.getId(), req.getAssessmentType());
                if (exists) throw new IllegalStateException(
                        "Only one " + req.getAssessmentType() + " per (class, subject, term)");
            }
            default -> {}
        }

        var a = new Assessment();
        a.setType(req.getAssessmentType());
        a.setWeight(req.getWeight());
        a.setDescription(req.getDescription());
        a.setClassRoom(cls);
        a.setSubject(subj);
        a.setTerm(term);
        a.setExamDate(req.getExamDate());
        return toDto(assessmentRepo.save(a));
    }

    @Transactional
    public AssessmentResp update(UUID id, AssessmentReq req){
        var a = assessmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found: " + id));

        var cls  = classRepo.findById(req.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found: " + req.getClassId()));
        var subj = subjectRepo.findById(req.getSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + req.getSubjectId()));
        var term = termRepo.findById(req.getTermId())
                .orElseThrow(() -> new EntityNotFoundException("Term not found: " + req.getTermId()));

        // đếm loại trừ chính bản ghi
        long others;
        switch (req.getAssessmentType()) {
            case ASSIGNMENT -> {
                others = em.createQuery("""
                      select count(x) from Assessment x
                      where x.classRoom.id = :c and x.subject.id = :s and x.term.id = :t
                        and x.type = :ty and x.id <> :id
                    """, Long.class)
                        .setParameter("c", cls.getId())
                        .setParameter("s", subj.getId())
                        .setParameter("t", term.getId())
                        .setParameter("ty", AssessmentType.ASSIGNMENT)
                        .setParameter("id", a.getId())
                        .getSingleResult();
                if (others >= 2)
                    throw new IllegalStateException("At most 2 ASSIGNMENT per (class, subject, term)");
            }
            case MIDTERM, FINAL -> {
                others = em.createQuery("""
                      select count(x) from Assessment x
                      where x.classRoom.id = :c and x.subject.id = :s and x.term.id = :t
                        and x.type = :ty and x.id <> :id
                    """, Long.class)
                        .setParameter("c", cls.getId())
                        .setParameter("s", subj.getId())
                        .setParameter("t", term.getId())
                        .setParameter("ty", req.getAssessmentType())
                        .setParameter("id", a.getId())
                        .getSingleResult();
                if (others > 0)
                    throw new IllegalStateException("Only one " + req.getAssessmentType() + " per (class, subject, term)");
            }
            default -> {}
        }

        a.setType(req.getAssessmentType());
        a.setWeight(req.getWeight());
        a.setDescription(req.getDescription());
        a.setClassRoom(cls);
        a.setSubject(subj);
        a.setTerm(term);
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
