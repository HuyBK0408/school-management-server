package huy.example.demoMonday.service;


import huy.example.demoMonday.dto.response.UserAccountResp;
import huy.example.demoMonday.entity.UserAccount;
import huy.example.demoMonday.repository.SchoolRepository;
import huy.example.demoMonday.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserAccountService {
    private final UserAccountRepository repo;
    private final SchoolRepository schoolRepo;
    private final PasswordEncoder passwordEncoder;
    //private final huy.example.demoMonday.security.PasswordSaltUtils saltUtils = new huy.example.demoMonday.security.PasswordSaltUtils();


    public UserAccountService(UserAccountRepository repo, SchoolRepository schoolRepo, PasswordEncoder pe) {
        this.repo = repo; this.schoolRepo = schoolRepo; this.passwordEncoder = pe;
    }


    private UserAccountResp toDto(UserAccount e){
        var b = new huy.example.demoMonday.dto.response.UserAccountResp();
        b.setId(e.getId()); b.setUsername(e.getUsername()); b.setEmail(e.getEmail());
        b.setPhone(e.getPhone()); b.setEnabled(e.isEnabled());
        b.setSchoolId(e.getSchool()!=null?e.getSchool().getId():null);
        return b;
    }


    @Transactional
    public huy.example.demoMonday.dto.response.UserAccountResp create(huy.example.demoMonday.dto.request.UserAccountReq req){
        var e = new UserAccount();
        e.setUsername(req.getUsername()); e.setEmail(req.getEmail()); e.setPhone(req.getPhone()); e.setEnabled(req.getEnabled());
        if (req.getSchoolId()!=null) e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow());
        if (req.getNewPassword()!=null && !req.getNewPassword().isBlank()) {
            String salt = huy.example.demoMonday.security.PasswordSaltUtils.newUserSaltBase64(32);
            e.setPasswordSalt(salt);
            e.setPasswordHash(passwordEncoder.encode(req.getNewPassword() + salt));
        }
        return toDto(repo.save(e));
    }


    @Transactional
    public huy.example.demoMonday.dto.response.UserAccountResp update(java.util.UUID id, huy.example.demoMonday.dto.request.UserAccountReq req){
        var e = repo.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("UserAccount not found: " + id));
        e.setUsername(req.getUsername()); e.setEmail(req.getEmail()); e.setPhone(req.getPhone()); e.setEnabled(req.getEnabled());
        if (req.getSchoolId()!=null) e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow());
        if (req.getNewPassword()!=null && !req.getNewPassword().isBlank()) {
            String salt = huy.example.demoMonday.security.PasswordSaltUtils.newUserSaltBase64(32);
            e.setPasswordSalt(salt);
            e.setPasswordHash(passwordEncoder.encode(req.getNewPassword() + salt));
        }
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