package Huy.example.demoMonday.service;

import Huy.example.demoMonday.entity.Subject;
import Huy.example.demoMonday.repo.SchoolRepository;
import Huy.example.demoMonday.repo.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubjectService {
    private final SubjectRepository repo;
    private final SchoolRepository schoolRepo;
    public SubjectService(SubjectRepository repo, SchoolRepository schoolRepo) {
        this.repo = repo; this.schoolRepo = schoolRepo;
    }

    private Huy.example.demoMonday.dto.response.SubjectResp toDto(Subject e){
        var b = Huy.example.demoMonday.dto.response.SubjectResp.builder().id(e.getId());
        b.name(e.getName()).code(e.getCode()).schoolId(e.getSchool().getId());
        return b.build();
    }

    @Transactional
    public Huy.example.demoMonday.dto.response.SubjectResp create(Huy.example.demoMonday.dto.request.SubjectReq req){
        var e = new Subject();
        e.setName(req.getName()); e.setCode(req.getCode());
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        return toDto(repo.save(e));
    }

    @Transactional
    public Huy.example.demoMonday.dto.response.SubjectResp update(java.util.UUID id, Huy.example.demoMonday.dto.request.SubjectReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Subject not found: " + id));
        e.setName(req.getName()); e.setCode(req.getCode());
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        return toDto(repo.save(e));
    }

    public Huy.example.demoMonday.dto.response.SubjectResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Subject not found: " + id));
        return toDto(e);
    }

    public java.util.List<Huy.example.demoMonday.dto.response.SubjectResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<Huy.example.demoMonday.dto.response.SubjectResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

