package Huy.example.demoMonday.service;

import Huy.example.demoMonday.entity.UserRole;
import Huy.example.demoMonday.repo.RoleRepository;
import Huy.example.demoMonday.repo.UserAccountRepository;
import Huy.example.demoMonday.repo.UserRoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRoleService {
    private final UserRoleRepository repo;
    private final UserAccountRepository userAccountRepo;
    private final RoleRepository roleRepo;
    public UserRoleService(UserRoleRepository repo, UserAccountRepository userAccountRepo, RoleRepository roleRepo) {
        this.repo = repo; this.userAccountRepo = userAccountRepo; this.roleRepo = roleRepo;
    }

    private Huy.example.demoMonday.dto.response.UserRoleResp toDto(UserRole e){
        var b = Huy.example.demoMonday.dto.response.UserRoleResp.builder().id(e.getId());
        b.userId(e.getUser().getId()).roleId(e.getRole().getId());
        return b.build();
    }

    @Transactional
    public Huy.example.demoMonday.dto.response.UserRoleResp create(Huy.example.demoMonday.dto.request.UserRoleReq req){
        var e = new UserRole();
        e.setUser(userAccountRepo.findById(req.getUserId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found: "+req.getUserId())));
        e.setRole(roleRepo.findById(req.getRoleId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Role not found: "+req.getRoleId())));
        return toDto(repo.save(e));
    }

    @Transactional
    public Huy.example.demoMonday.dto.response.UserRoleResp update(java.util.UUID id, Huy.example.demoMonday.dto.request.UserRoleReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("UserRole not found: " + id));
        e.setUser(userAccountRepo.findById(req.getUserId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found: "+req.getUserId())));
        e.setRole(roleRepo.findById(req.getRoleId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Role not found: "+req.getRoleId())));
        return toDto(repo.save(e));
    }

    public Huy.example.demoMonday.dto.response.UserRoleResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("UserRole not found: " + id));
        return toDto(e);
    }

    public java.util.List<Huy.example.demoMonday.dto.response.UserRoleResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<Huy.example.demoMonday.dto.response.UserRoleResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

