package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.StudentParent;
import huy.example.demoMonday.repository.ParentRepository;
import huy.example.demoMonday.repository.StudentParentRepository;
import huy.example.demoMonday.repository.StudentRepository;
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

    private huy.example.demoMonday.dto.response.StudentParentResp toDto(StudentParent e){
        var b = huy.example.demoMonday.dto.response.StudentParentResp.builder().id(e.getId());
        b.studentId(e.getStudent().getId()).parentId(e.getParent().getId());
        return b.build();
    }

    @Transactional
    public huy.example.demoMonday.dto.response.StudentParentResp create(huy.example.demoMonday.dto.request.StudentParentReq req){
        var e = new StudentParent();
        e.setStudent(studentRepo.findById(req.getStudentId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Student not found: "+req.getStudentId())));
        e.setParent(parentRepo.findById(req.getParentId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Parent not found: "+req.getParentId())));
        return toDto(repo.save(e));
    }

    @Transactional
    public huy.example.demoMonday.dto.response.StudentParentResp update(java.util.UUID id, huy.example.demoMonday.dto.request.StudentParentReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("StudentParent not found: " + id));
        e.setStudent(studentRepo.findById(req.getStudentId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Student not found: "+req.getStudentId())));
        e.setParent(parentRepo.findById(req.getParentId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Parent not found: "+req.getParentId())));
        return toDto(repo.save(e));
    }

    public huy.example.demoMonday.dto.response.StudentParentResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("StudentParent not found: " + id));
        return toDto(e);
    }

    public java.util.List<huy.example.demoMonday.dto.response.StudentParentResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<huy.example.demoMonday.dto.response.StudentParentResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

