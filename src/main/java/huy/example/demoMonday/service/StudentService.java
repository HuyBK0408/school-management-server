package huy.example.demoMonday.service;

import huy.example.demoMonday.dto.request.AdminCreateUserReq;
import huy.example.demoMonday.entity.Student;
import huy.example.demoMonday.repository.ClassRoomRepository;
import huy.example.demoMonday.repository.SchoolRepository;
import huy.example.demoMonday.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class StudentService {
    private final StudentRepository repo;
    private final ClassRoomRepository classRoomRepo;
    private final SchoolRepository schoolRepo;
    private final AuthService authService;
    private final UserPhotoService userPhotoService;

    public StudentService(StudentRepository repo, ClassRoomRepository classRoomRepo, SchoolRepository schoolRepo, AuthService authService, UserPhotoService userPhotoService) {
        this.repo = repo; this.classRoomRepo = classRoomRepo; this.schoolRepo = schoolRepo; this.authService = authService; this.userPhotoService = userPhotoService;
    }

    private huy.example.demoMonday.dto.response.StudentResp toDto(Student e){
        var b = huy.example.demoMonday.dto.response.StudentResp.builder().id(e.getId());
        b.fullName(e.getFullName()).dob(e.getDob()).gender(e.getGender()).studentCode(e.getStudentCode());
        b.currentClassId(e.getCurrentClass()==null?null:e.getCurrentClass().getId());
        b.schoolId(e.getSchool().getId()).photoUrl(e.getPhotoUrl()).status(e.getStatus());
        return b.build();
    }

    @Transactional
    public huy.example.demoMonday.dto.response.StudentResp create(huy.example.demoMonday.dto.request.StudentReq req){
        var e = new Student();
        e.setFullName(req.getFullName()); e.setDob(req.getDob()); e.setGender(req.getGender()); e.setStudentCode(req.getStudentCode());
        e.setCurrentClass(req.getCurrentClassId()==null?null:classRoomRepo.findById(req.getCurrentClassId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Class not found: "+req.getCurrentClassId())));
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        e.setPhotoUrl(req.getPhotoUrl()); e.setStatus(req.getStatus());
        return toDto(repo.save(e));
    }

    @Transactional
    public huy.example.demoMonday.dto.response.StudentResp update(java.util.UUID id, huy.example.demoMonday.dto.request.StudentReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
        e.setFullName(req.getFullName()); e.setDob(req.getDob()); e.setGender(req.getGender()); e.setStudentCode(req.getStudentCode());
        e.setCurrentClass(req.getCurrentClassId()==null?null:classRoomRepo.findById(req.getCurrentClassId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Class not found: "+req.getCurrentClassId())));
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        e.setPhotoUrl(req.getPhotoUrl()); e.setStatus(req.getStatus());
        return toDto(repo.save(e));
    }

    public huy.example.demoMonday.dto.response.StudentResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
        return toDto(e);
    }

    public java.util.List<huy.example.demoMonday.dto.response.StudentResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<huy.example.demoMonday.dto.response.StudentResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public void createAccount(UUID studentId, AdminCreateUserReq req, MultipartFile photo) {
        Student s = repo.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học sinh"));

        if (s.getUser() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Học sinh đã có tài khoản");
        }

        // role phải là STUDENT (nếu client gửi khác -> 400)
        if (req.getRoleCode() != null && !req.getRoleCode().equalsIgnoreCase("STUDENT")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roleCode phải là STUDENT cho endpoint này");
        }

        var user = authService.registerUser(req.getUsername(), req.getEmail(), req.getPassword(), "STUDENT");

        s.setUser(user);
        repo.save(s);

        if (photo != null && !photo.isEmpty()) {
            userPhotoService.saveAndAttachToUser(user.getId(), photo);
        }
    }

}
