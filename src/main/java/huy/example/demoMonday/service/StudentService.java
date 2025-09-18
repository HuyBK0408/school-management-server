package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.Student;
import huy.example.demoMonday.repository.ClassRoomRepository;
import huy.example.demoMonday.repository.SchoolRepository;
import huy.example.demoMonday.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {
    private final StudentRepository repo;
    private final ClassRoomRepository classRoomRepo;
    private final SchoolRepository schoolRepo;
    public StudentService(StudentRepository repo, ClassRoomRepository classRoomRepo, SchoolRepository schoolRepo) {
        this.repo = repo; this.classRoomRepo = classRoomRepo; this.schoolRepo = schoolRepo;
    }

    private huy.example.demoMonday.dto.response.StudentResp toDto(Student e){
        var b = huy.example.demoMonday.dto.response.StudentResp.builder().id(e.getId());
        b.fullName(e.getFullName()).dob(e.getDob()).gender(e.getGender()).studentCode(e.getStudentCode());
        b.currentClassId(e.getCurrentClass()==null?null:e.getCurrentClass().getId());
        b.schoolId(e.getSchool().getId()).photoUrl(e.getPhotoUrl()).status(e.getStatus());
        return b.build();
    }

    @Transactional
    public huy.example.demoMonday.dto.response.StudentResp create(huy.example.demoMonday.dto.request.StudentReq req){
        var e = new Student();
        e.setFullName(req.getFullName()); e.setDob(req.getDob()); e.setGender(req.getGender()); e.setStudentCode(req.getStudentCode());
        e.setCurrentClass(req.getCurrentClassId()==null?null:classRoomRepo.findById(req.getCurrentClassId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Class not found: "+req.getCurrentClassId())));
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        e.setPhotoUrl(req.getPhotoUrl()); e.setStatus(req.getStatus());
        return toDto(repo.save(e));
    }

    @Transactional
    public huy.example.demoMonday.dto.response.StudentResp update(java.util.UUID id, huy.example.demoMonday.dto.request.StudentReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
        e.setFullName(req.getFullName()); e.setDob(req.getDob()); e.setGender(req.getGender()); e.setStudentCode(req.getStudentCode());
        e.setCurrentClass(req.getCurrentClassId()==null?null:classRoomRepo.findById(req.getCurrentClassId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Class not found: "+req.getCurrentClassId())));
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        e.setPhotoUrl(req.getPhotoUrl()); e.setStatus(req.getStatus());
        return toDto(repo.save(e));
    }

    public huy.example.demoMonday.dto.response.StudentResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
        return toDto(e);
    }

    public java.util.List<huy.example.demoMonday.dto.response.StudentResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<huy.example.demoMonday.dto.response.StudentResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}
