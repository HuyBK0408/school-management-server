package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.School;
import huy.example.demoMonday.repository.SchoolRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolService {
    private final SchoolRepository repo;
    public SchoolService(SchoolRepository repo) {
        this.repo = repo;
    }

    private huy.example.demoMonday.dto.response.SchoolResp toDto(School e){
        var b = huy.example.demoMonday.dto.response.SchoolResp.builder().id(e.getId());
        b.name(e.getName()).address(e.getAddress()).phone(e.getPhone()).email(e.getEmail());
        return b.build();
    }

    @Transactional
    public huy.example.demoMonday.dto.response.SchoolResp create(huy.example.demoMonday.dto.request.SchoolReq req){
        var e = new School();
        e.setName(req.getName()); e.setAddress(req.getAddress()); e.setPhone(req.getPhone()); e.setEmail(req.getEmail());
        return toDto(repo.save(e));
    }

    @Transactional
    public huy.example.demoMonday.dto.response.SchoolResp update(java.util.UUID id, huy.example.demoMonday.dto.request.SchoolReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("School not found: " + id));
        e.setName(req.getName()); e.setAddress(req.getAddress()); e.setPhone(req.getPhone()); e.setEmail(req.getEmail());
        return toDto(repo.save(e));
    }

    public huy.example.demoMonday.dto.response.SchoolResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("School not found: " + id));
        return toDto(e);
    }

    public java.util.List<huy.example.demoMonday.dto.response.SchoolResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<huy.example.demoMonday.dto.response.SchoolResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

