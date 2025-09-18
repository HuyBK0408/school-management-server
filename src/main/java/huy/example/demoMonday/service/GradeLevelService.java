package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.GradeLevel;
import huy.example.demoMonday.repository.GradeLevelRepository;
import huy.example.demoMonday.repository.SchoolRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GradeLevelService {
    private final GradeLevelRepository repo;
    private final SchoolRepository schoolRepo;
    public GradeLevelService(GradeLevelRepository repo, SchoolRepository schoolRepo) {
        this.repo = repo; this.schoolRepo = schoolRepo;
    }

    private huy.example.demoMonday.dto.response.GradeLevelResp toDto(GradeLevel e){
        var b = huy.example.demoMonday.dto.response.GradeLevelResp.builder().id(e.getId());
        b.name(e.getName()).schoolId(e.getSchool().getId());
        return b.build();
    }

    @Transactional
    public huy.example.demoMonday.dto.response.GradeLevelResp create(huy.example.demoMonday.dto.request.GradeLevelReq req){
        var e = new GradeLevel();
        e.setName(req.getName());
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        return toDto(repo.save(e));
    }

    @Transactional
    public huy.example.demoMonday.dto.response.GradeLevelResp update(java.util.UUID id, huy.example.demoMonday.dto.request.GradeLevelReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("GradeLevel not found: " + id));
        e.setName(req.getName());
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        return toDto(repo.save(e));
    }

    public huy.example.demoMonday.dto.response.GradeLevelResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("GradeLevel not found: " + id));
        return toDto(e);
    }

    public java.util.List<huy.example.demoMonday.dto.response.GradeLevelResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<huy.example.demoMonday.dto.response.GradeLevelResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

