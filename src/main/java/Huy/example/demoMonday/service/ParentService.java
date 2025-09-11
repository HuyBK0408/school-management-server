package Huy.example.demoMonday.service;

import Huy.example.demoMonday.entity.Parent;
import Huy.example.demoMonday.repo.ParentRepository;
import Huy.example.demoMonday.repo.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParentService {
    private final ParentRepository repo;
    private final UserAccountRepository userAccountRepo;
    public ParentService(ParentRepository repo, UserAccountRepository userAccountRepo) {
        this.repo = repo; this.userAccountRepo = userAccountRepo;
    }

    private Huy.example.demoMonday.dto.response.ParentResp toDto(Parent e){
        var b = Huy.example.demoMonday.dto.response.ParentResp.builder().id(e.getId());
        b.fullName(e.getFullName()).relationType(e.getRelationType()).phone(e.getPhone()).email(e.getEmail()).address(e.getAddress());
        b.userId(e.getUser()==null?null:e.getUser().getId());
        return b.build();
    }

    @Transactional
    public Huy.example.demoMonday.dto.response.ParentResp create(Huy.example.demoMonday.dto.request.ParentReq req){
        var e = new Parent();
        e.setFullName(req.getFullName()); e.setRelationType(req.getRelationType()); e.setPhone(req.getPhone()); e.setEmail(req.getEmail()); e.setAddress(req.getAddress());
        e.setUser(req.getUserId()==null?null:userAccountRepo.findById(req.getUserId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found: "+req.getUserId())));
        return toDto(repo.save(e));
    }

    @Transactional
    public Huy.example.demoMonday.dto.response.ParentResp update(java.util.UUID id, Huy.example.demoMonday.dto.request.ParentReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Parent not found: " + id));
        e.setFullName(req.getFullName()); e.setRelationType(req.getRelationType()); e.setPhone(req.getPhone()); e.setEmail(req.getEmail()); e.setAddress(req.getAddress());
        e.setUser(req.getUserId()==null?null:userAccountRepo.findById(req.getUserId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found: "+req.getUserId())));
        return toDto(repo.save(e));
    }

    public Huy.example.demoMonday.dto.response.ParentResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Parent not found: " + id));
        return toDto(e);
    }

    public java.util.List<Huy.example.demoMonday.dto.response.ParentResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<Huy.example.demoMonday.dto.response.ParentResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

