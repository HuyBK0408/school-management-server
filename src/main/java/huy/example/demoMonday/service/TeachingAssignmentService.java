package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.TeachingAssignment;
import huy.example.demoMonday.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeachingAssignmentService {
    private final TeachingAssignmentRepository repo;
    private final StaffRepository staffRepo;
    private final ClassRoomRepository classRoomRepo;
    private final SubjectRepository subjectRepo;
    private final SchoolYearRepository schoolYearRepo;
    public TeachingAssignmentService(TeachingAssignmentRepository repo, StaffRepository staffRepo, ClassRoomRepository classRoomRepo, SubjectRepository subjectRepo, SchoolYearRepository schoolYearRepo) {
        this.repo = repo; this.staffRepo = staffRepo; this.classRoomRepo = classRoomRepo; this.subjectRepo = subjectRepo; this.schoolYearRepo = schoolYearRepo;
    }

    private huy.example.demoMonday.dto.response.TeachingAssignmentResp toDto(TeachingAssignment e){
        var b = huy.example.demoMonday.dto.response.TeachingAssignmentResp.builder().id(e.getId());
        b.teacherId(e.getTeacher().getId()).classId(e.getClassRoom().getId()).subjectId(e.getSubject().getId()).schoolYearId(e.getSchoolYear().getId());
        b.assignedFrom(e.getAssignedFrom()).assignedTo(e.getAssignedTo());
        return b.build();
    }

    @Transactional
    public huy.example.demoMonday.dto.response.TeachingAssignmentResp create(huy.example.demoMonday.dto.request.TeachingAssignmentReq req){
        var e = new TeachingAssignment();
        e.setTeacher(staffRepo.findById(req.getTeacherId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Staff not found: "+req.getTeacherId())));
        e.setClassRoom(classRoomRepo.findById(req.getClassId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Class not found: "+req.getClassId())));
        e.setSubject(subjectRepo.findById(req.getSubjectId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Subject not found: "+req.getSubjectId())));
        e.setSchoolYear(schoolYearRepo.findById(req.getSchoolYearId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("SchoolYear not found: "+req.getSchoolYearId())));
        e.setAssignedFrom(req.getAssignedFrom()); e.setAssignedTo(req.getAssignedTo());
        return toDto(repo.save(e));
    }

    @Transactional
    public huy.example.demoMonday.dto.response.TeachingAssignmentResp update(java.util.UUID id, huy.example.demoMonday.dto.request.TeachingAssignmentReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("TeachingAssignment not found: " + id));
        e.setTeacher(staffRepo.findById(req.getTeacherId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Staff not found: "+req.getTeacherId())));
        e.setClassRoom(classRoomRepo.findById(req.getClassId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Class not found: "+req.getClassId())));
        e.setSubject(subjectRepo.findById(req.getSubjectId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Subject not found: "+req.getSubjectId())));
        e.setSchoolYear(schoolYearRepo.findById(req.getSchoolYearId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("SchoolYear not found: "+req.getSchoolYearId())));
        e.setAssignedFrom(req.getAssignedFrom()); e.setAssignedTo(req.getAssignedTo());
        return toDto(repo.save(e));
    }

    public huy.example.demoMonday.dto.response.TeachingAssignmentResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("TeachingAssignment not found: " + id));
        return toDto(e);
    }

    public java.util.List<huy.example.demoMonday.dto.response.TeachingAssignmentResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<huy.example.demoMonday.dto.response.TeachingAssignmentResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

