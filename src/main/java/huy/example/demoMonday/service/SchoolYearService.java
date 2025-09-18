package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.SchoolYear;
import huy.example.demoMonday.repository.SchoolRepository;
import huy.example.demoMonday.repository.SchoolYearRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolYearService {
    private final SchoolYearRepository repo;
    private final SchoolRepository schoolRepo;
    public SchoolYearService(SchoolYearRepository repo, SchoolRepository schoolRepo) {
        this.repo = repo; this.schoolRepo = schoolRepo;
    }

    private huy.example.demoMonday.dto.response.SchoolYearResp toDto(SchoolYear e){
        var b = huy.example.demoMonday.dto.response.SchoolYearResp.builder().id(e.getId());
        b.code(e.getCode()).startDate(e.getStartDate()).endDate(e.getEndDate()).schoolId(e.getSchool().getId());
        return b.build();
    }

    @Transactional
    public huy.example.demoMonday.dto.response.SchoolYearResp create(huy.example.demoMonday.dto.request.SchoolYearReq req){
        var e = new SchoolYear();
        e.setCode(req.getCode()); e.setStartDate(req.getStartDate()); e.setEndDate(req.getEndDate());
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        return toDto(repo.save(e));
    }

    @Transactional
    public huy.example.demoMonday.dto.response.SchoolYearResp update(java.util.UUID id, huy.example.demoMonday.dto.request.SchoolYearReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("SchoolYear not found: " + id));
        e.setCode(req.getCode()); e.setStartDate(req.getStartDate()); e.setEndDate(req.getEndDate());
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        return toDto(repo.save(e));
    }

    public huy.example.demoMonday.dto.response.SchoolYearResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("SchoolYear not found: " + id));
        return toDto(e);
    }

    public java.util.List<huy.example.demoMonday.dto.response.SchoolYearResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<huy.example.demoMonday.dto.response.SchoolYearResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

