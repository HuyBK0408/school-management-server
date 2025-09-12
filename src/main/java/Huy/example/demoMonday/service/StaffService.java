package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.Staff;
import huy.example.demoMonday.repo.SchoolRepository;
import huy.example.demoMonday.repo.StaffRepository;
import huy.example.demoMonday.repo.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffService {
    private final StaffRepository repo;
    private final SchoolRepository schoolRepo;
    private final UserAccountRepository userAccountRepo;
    public StaffService(StaffRepository repo, SchoolRepository schoolRepo, UserAccountRepository userAccountRepo) {
        this.repo = repo; this.schoolRepo = schoolRepo; this.userAccountRepo = userAccountRepo;
    }

    private huy.example.demoMonday.dto.response.StaffResp toDto(Staff e){
        var b = huy.example.demoMonday.dto.response.StaffResp.builder().id(e.getId());
        b.fullName(e.getFullName()).dob(e.getDob()).gender(e.getGender()).phone(e.getPhone()).email(e.getEmail()).position(e.getPosition());
        b.schoolId(e.getSchool().getId());
        b.userId(e.getUser()==null?null:e.getUser().getId());
        return b.build();
    }

    @Transactional
    public huy.example.demoMonday.dto.response.StaffResp create(huy.example.demoMonday.dto.request.StaffReq req){
        var e = new Staff();
        e.setFullName(req.getFullName()); e.setDob(req.getDob()); e.setGender(req.getGender()); e.setPhone(req.getPhone()); e.setEmail(req.getEmail()); e.setPosition(req.getPosition());
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        e.setUser(req.getUserId()==null?null:userAccountRepo.findById(req.getUserId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found: "+req.getUserId())));
        return toDto(repo.save(e));
    }

    @Transactional
    public huy.example.demoMonday.dto.response.StaffResp update(java.util.UUID id, huy.example.demoMonday.dto.request.StaffReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Staff not found: " + id));
        e.setFullName(req.getFullName()); e.setDob(req.getDob()); e.setGender(req.getGender()); e.setPhone(req.getPhone()); e.setEmail(req.getEmail()); e.setPosition(req.getPosition());
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        e.setUser(req.getUserId()==null?null:userAccountRepo.findById(req.getUserId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found: "+req.getUserId())));
        return toDto(repo.save(e));
    }

    public huy.example.demoMonday.dto.response.StaffResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Staff not found: " + id));
        return toDto(e);
    }

    public java.util.List<huy.example.demoMonday.dto.response.StaffResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<huy.example.demoMonday.dto.response.StaffResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}
