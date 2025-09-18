package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.Subject;
import huy.example.demoMonday.repository.SchoolRepository;
import huy.example.demoMonday.repository.SubjectRepository;
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

    private huy.example.demoMonday.dto.response.SubjectResp toDto(Subject e){
        var b = huy.example.demoMonday.dto.response.SubjectResp.builder().id(e.getId());
        b.name(e.getName()).code(e.getCode()).schoolId(e.getSchool().getId());
        return b.build();
    }

    @Transactional
    public huy.example.demoMonday.dto.response.SubjectResp create(huy.example.demoMonday.dto.request.SubjectReq req){
        var e = new Subject();
        e.setName(req.getName()); e.setCode(req.getCode());
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        return toDto(repo.save(e));
    }

    @Transactional
    public huy.example.demoMonday.dto.response.SubjectResp update(java.util.UUID id, huy.example.demoMonday.dto.request.SubjectReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Subject not found: " + id));
        e.setName(req.getName()); e.setCode(req.getCode());
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        return toDto(repo.save(e));
    }

    public huy.example.demoMonday.dto.response.SubjectResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Subject not found: " + id));
        return toDto(e);
    }

    public java.util.List<huy.example.demoMonday.dto.response.SubjectResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<huy.example.demoMonday.dto.response.SubjectResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

