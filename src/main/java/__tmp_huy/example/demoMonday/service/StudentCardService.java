package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.StudentCard;
import huy.example.demoMonday.repo.StudentCardRepository;
import huy.example.demoMonday.repo.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentCardService {
    private final StudentCardRepository repo;
    private final StudentRepository studentRepo;
    public StudentCardService(StudentCardRepository repo, StudentRepository studentRepo) {
        this.repo = repo; this.studentRepo = studentRepo;
    }

    private huy.example.demoMonday.dto.response.StudentCardResp toDto(StudentCard e){
        var b = huy.example.demoMonday.dto.response.StudentCardResp.builder().id(e.getId());
        b.studentId(e.getStudent().getId()).cardNumber(e.getCardNumber()).issuedDate(e.getIssuedDate()).expiredDate(e.getExpiredDate()).status(e.getStatus());
        return b.build();
    }

    @Transactional
    public huy.example.demoMonday.dto.response.StudentCardResp create(huy.example.demoMonday.dto.request.StudentCardReq req){
        var e = new StudentCard();
        e.setStudent(studentRepo.findById(req.getStudentId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Student not found: "+req.getStudentId())));
        e.setCardNumber(req.getCardNumber()); e.setIssuedDate(req.getIssuedDate()); e.setExpiredDate(req.getExpiredDate()); e.setStatus(req.getStatus());
        return toDto(repo.save(e));
    }

    @Transactional
    public huy.example.demoMonday.dto.response.StudentCardResp update(java.util.UUID id, huy.example.demoMonday.dto.request.StudentCardReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("StudentCard not found: " + id));
        e.setStudent(studentRepo.findById(req.getStudentId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Student not found: "+req.getStudentId())));
        e.setCardNumber(req.getCardNumber()); e.setIssuedDate(req.getIssuedDate()); e.setExpiredDate(req.getExpiredDate()); e.setStatus(req.getStatus());
        return toDto(repo.save(e));
    }

    public huy.example.demoMonday.dto.response.StudentCardResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("StudentCard not found: " + id));
        return toDto(e);
    }

    public java.util.List<huy.example.demoMonday.dto.response.StudentCardResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<huy.example.demoMonday.dto.response.StudentCardResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

