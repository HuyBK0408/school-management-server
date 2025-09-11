package Huy.example.demoMonday.service;

import Huy.example.demoMonday.entity.Role;
import Huy.example.demoMonday.repo.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {
    private final RoleRepository repo;
    public RoleService(RoleRepository repo) {
        this.repo = repo;
    }

    private Huy.example.demoMonday.dto.response.RoleResp toDto(Role e){
        var b = Huy.example.demoMonday.dto.response.RoleResp.builder().id(e.getId());
        b.code(e.getCode());
        return b.build();
    }

    @Transactional
    public Huy.example.demoMonday.dto.response.RoleResp create(Huy.example.demoMonday.dto.request.RoleReq req){
        var e = new Role();
        e.setCode(req.getCode());
        return toDto(repo.save(e));
    }

    @Transactional
    public Huy.example.demoMonday.dto.response.RoleResp update(java.util.UUID id, Huy.example.demoMonday.dto.request.RoleReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
        e.setCode(req.getCode());
        return toDto(repo.save(e));
    }

    public Huy.example.demoMonday.dto.response.RoleResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
        return toDto(e);
    }

    public java.util.List<Huy.example.demoMonday.dto.response.RoleResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<Huy.example.demoMonday.dto.response.RoleResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

