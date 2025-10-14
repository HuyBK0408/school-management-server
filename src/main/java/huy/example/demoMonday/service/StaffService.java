package huy.example.demoMonday.service;

import huy.example.demoMonday.dto.request.AdminCreateUserReq;
import huy.example.demoMonday.entity.Staff;
import huy.example.demoMonday.repository.SchoolRepository;
import huy.example.demoMonday.repository.StaffRepository;
import huy.example.demoMonday.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class StaffService {
    private final StaffRepository repo;
    private final SchoolRepository schoolRepo;
    private final UserAccountRepository userAccountRepo;
    private final AuthService authService;
    private final UserPhotoService userPhotoService;
    public StaffService(StaffRepository repo, SchoolRepository schoolRepo, UserAccountRepository userAccountRepo, AuthService authService, UserPhotoService userPhotoService) {
        this.repo = repo; this.schoolRepo = schoolRepo; this.userAccountRepo = userAccountRepo; this.authService = authService; this.userPhotoService = userPhotoService;
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

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public void createAccount(UUID staffId, AdminCreateUserReq req, MultipartFile photo) {
        Staff st = repo.findById(staffId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giáo viên/nhân sự"));

        if (st.getUser() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nhân sự đã có tài khoản");
        }
        if (req.getRoleCode() != null && !req.getRoleCode().equalsIgnoreCase("TEACHER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roleCode phải là TEACHER cho endpoint này");
        }

        var user = authService.registerUser(req.getUsername(), req.getEmail(), req.getPassword(), "TEACHER");
        st.setUser(user);
        repo.save(st);

        if (photo != null && !photo.isEmpty()) {
            userPhotoService.saveAndAttachToUser(user.getId(), photo);
        }
    }

}
