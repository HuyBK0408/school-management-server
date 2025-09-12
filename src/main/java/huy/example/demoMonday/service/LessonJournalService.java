package huy.example.demoMonday.service;


import huy.example.demoMonday.entity.LessonJournal;
import huy.example.demoMonday.repo.ClassRoomRepository;
import huy.example.demoMonday.repo.LessonJournalRepository;
import huy.example.demoMonday.repo.StaffRepository;
import huy.example.demoMonday.repo.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LessonJournalService {
    private final LessonJournalRepository repo;
    private final ClassRoomRepository classRoomRepo;
    private final SubjectRepository subjectRepo;
    private final StaffRepository staffRepo;
    public LessonJournalService(LessonJournalRepository repo, ClassRoomRepository classRoomRepo, SubjectRepository subjectRepo, StaffRepository staffRepo) {
        this.repo = repo; this.classRoomRepo = classRoomRepo; this.subjectRepo = subjectRepo; this.staffRepo = staffRepo;
    }

    private huy.example.demoMonday.dto.response.LessonJournalResp toDto(LessonJournal e){
        var b = huy.example.demoMonday.dto.response.LessonJournalResp.builder().id(e.getId());
        b.classId(e.getClassRoom().getId()).subjectId(e.getSubject().getId()).teacherId(e.getTeacher().getId());
        b.teachDate(e.getTeachDate()).periodNo(e.getPeriodNo()).content(e.getContent());
        b.transferredFromTeacherId(e.getTransferredFromTeacher()==null?null:e.getTransferredFromTeacher().getId());
        return b.build();
    }

    @Transactional
    public huy.example.demoMonday.dto.response.LessonJournalResp create(huy.example.demoMonday.dto.request.LessonJournalReq req){
        var e = new LessonJournal();
        e.setClassRoom(classRoomRepo.findById(req.getClassId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Class not found: "+req.getClassId())));
        e.setSubject(subjectRepo.findById(req.getSubjectId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Subject not found: "+req.getSubjectId())));
        e.setTeacher(staffRepo.findById(req.getTeacherId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Staff not found: "+req.getTeacherId())));
        e.setTeachDate(req.getTeachDate()); e.setPeriodNo(req.getPeriodNo()); e.setContent(req.getContent());
        e.setTransferredFromTeacher(req.getTransferredFromTeacherId()==null?null:staffRepo.findById(req.getTransferredFromTeacherId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Staff not found: "+req.getTransferredFromTeacherId())));
        return toDto(repo.save(e));
    }

    @Transactional
    public huy.example.demoMonday.dto.response.LessonJournalResp update(java.util.UUID id, huy.example.demoMonday.dto.request.LessonJournalReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("LessonJournal not found: " + id));
        e.setClassRoom(classRoomRepo.findById(req.getClassId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Class not found: "+req.getClassId())));
        e.setSubject(subjectRepo.findById(req.getSubjectId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Subject not found: "+req.getSubjectId())));
        e.setTeacher(staffRepo.findById(req.getTeacherId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Staff not found: "+req.getTeacherId())));
        e.setTeachDate(req.getTeachDate()); e.setPeriodNo(req.getPeriodNo()); e.setContent(req.getContent());
        e.setTransferredFromTeacher(req.getTransferredFromTeacherId()==null?null:staffRepo.findById(req.getTransferredFromTeacherId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Staff not found: "+req.getTransferredFromTeacherId())));
        return toDto(repo.save(e));
    }

    public huy.example.demoMonday.dto.response.LessonJournalResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("LessonJournal not found: " + id));
        return toDto(e);
    }

    public java.util.List<huy.example.demoMonday.dto.response.LessonJournalResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<huy.example.demoMonday.dto.response.LessonJournalResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

