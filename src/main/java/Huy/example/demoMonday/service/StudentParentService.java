package Huy.example.demoMonday.service;

import Huy.example.demoMonday.entity.StudentParent;
import Huy.example.demoMonday.repo.ParentRepository;
import Huy.example.demoMonday.repo.StudentParentRepository;
import Huy.example.demoMonday.repo.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentParentService {
    private final StudentParentRepository repo;
    private final StudentRepository studentRepo;
    private final ParentRepository parentRepo;
    public StudentParentService(StudentParentRepository repo, StudentRepository studentRepo, ParentRepository parentRepo) {
        this.repo = repo; this.studentRepo = studentRepo; this.parentRepo = parentRepo;
    }

    private Huy.example.demoMonday.dto.response.StudentParentResp toDto(StudentParent e){
        var b = Huy.example.demoMonday.dto.response.StudentParentResp.builder().id(e.getId());
        b.studentId(e.getStudent().getId()).parentId(e.getParent().getId());
        return b.build();
    }

    @Transactional
    public Huy.example.demoMonday.dto.response.StudentParentResp create(Huy.example.demoMonday.dto.request.StudentParentReq req){
        var e = new StudentParent();
        e.setStudent(studentRepo.findById(req.getStudentId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Student not found: "+req.getStudentId())));
        e.setParent(parentRepo.findById(req.getParentId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Parent not found: "+req.getParentId())));
        return toDto(repo.save(e));
    }

    @Transactional
    public Huy.example.demoMonday.dto.response.StudentParentResp update(java.util.UUID id, Huy.example.demoMonday.dto.request.StudentParentReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("StudentParent not found: " + id));
        e.setStudent(studentRepo.findById(req.getStudentId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Student not found: "+req.getStudentId())));
        e.setParent(parentRepo.findById(req.getParentId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Parent not found: "+req.getParentId())));
        return toDto(repo.save(e));
    }

    public Huy.example.demoMonday.dto.response.StudentParentResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("StudentParent not found: " + id));
        return toDto(e);
    }

    public java.util.List<Huy.example.demoMonday.dto.response.StudentParentResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<Huy.example.demoMonday.dto.response.StudentParentResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

