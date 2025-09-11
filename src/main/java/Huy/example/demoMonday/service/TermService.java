package Huy.example.demoMonday.service;

import Huy.example.demoMonday.entity.Term;
import Huy.example.demoMonday.repo.SchoolYearRepository;
import Huy.example.demoMonday.repo.TermRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TermService {
    private final TermRepository repo;
    private final SchoolYearRepository schoolYearRepo;
    public TermService(TermRepository repo, SchoolYearRepository schoolYearRepo) {
        this.repo = repo; this.schoolYearRepo = schoolYearRepo;
    }

    private Huy.example.demoMonday.dto.response.TermResp toDto(Term e){
        var b = Huy.example.demoMonday.dto.response.TermResp.builder().id(e.getId());
        b.name(e.getName()).orderNo(e.getOrderNo()).schoolYearId(e.getSchoolYear().getId());
        return b.build();
    }

    @Transactional
    public Huy.example.demoMonday.dto.response.TermResp create(Huy.example.demoMonday.dto.request.TermReq req){
        var e = new Term();
        e.setName(req.getName()); e.setOrderNo(req.getOrderNo());
        e.setSchoolYear(schoolYearRepo.findById(req.getSchoolYearId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("SchoolYear not found: "+req.getSchoolYearId())));
        return toDto(repo.save(e));
    }

    @Transactional
    public Huy.example.demoMonday.dto.response.TermResp update(java.util.UUID id, Huy.example.demoMonday.dto.request.TermReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Term not found: " + id));
        e.setName(req.getName()); e.setOrderNo(req.getOrderNo());
        e.setSchoolYear(schoolYearRepo.findById(req.getSchoolYearId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("SchoolYear not found: "+req.getSchoolYearId())));
        return toDto(repo.save(e));
    }

    public Huy.example.demoMonday.dto.response.TermResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Term not found: " + id));
        return toDto(e);
    }

    public java.util.List<Huy.example.demoMonday.dto.response.TermResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<Huy.example.demoMonday.dto.response.TermResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

