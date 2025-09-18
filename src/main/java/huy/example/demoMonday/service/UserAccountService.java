package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.UserAccount;
import huy.example.demoMonday.repository.SchoolRepository;
import huy.example.demoMonday.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {
    private final UserAccountRepository repo;
    private final SchoolRepository schoolRepo;
    public UserAccountService(UserAccountRepository repo, SchoolRepository schoolRepo) {
        this.repo = repo; this.schoolRepo = schoolRepo;
    }

    private huy.example.demoMonday.dto.response.UserAccountResp toDto(UserAccount e){
        var b = huy.example.demoMonday.dto.response.UserAccountResp.builder().id(e.getId());
        b.username(e.getUsername()).email(e.getEmail()).phone(e.getPhone()).enabled(e.isEnabled());
        b.schoolId(e.getSchool()==null?null:e.getSchool().getId());
        return b.build();
    }

    @Transactional
    public huy.example.demoMonday.dto.response.UserAccountResp create(huy.example.demoMonday.dto.request.UserAccountReq req){
        var e = new UserAccount();
        e.setUsername(req.getUsername()); e.setPasswordHash(req.getPasswordHash()); e.setEmail(req.getEmail()); e.setPhone(req.getPhone()); e.setEnabled(req.getEnabled());
        e.setSchool(req.getSchoolId()==null?null:schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        return toDto(repo.save(e));
    }

    @Transactional
    public huy.example.demoMonday.dto.response.UserAccountResp update(java.util.UUID id, huy.example.demoMonday.dto.request.UserAccountReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("UserAccount not found: " + id));
        e.setUsername(req.getUsername()); e.setPasswordHash(req.getPasswordHash()); e.setEmail(req.getEmail()); e.setPhone(req.getPhone()); e.setEnabled(req.getEnabled());
        e.setSchool(req.getSchoolId()==null?null:schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        return toDto(repo.save(e));
    }

    public huy.example.demoMonday.dto.response.UserAccountResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("UserAccount not found: " + id));
        return toDto(e);
    }

    public java.util.List<huy.example.demoMonday.dto.response.UserAccountResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<huy.example.demoMonday.dto.response.UserAccountResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

