package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.ClassRoom;
import huy.example.demoMonday.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassRoomService {
    private final ClassRoomRepository repo;
    private final GradeLevelRepository gradeLevelRepo;
    private final SchoolYearRepository schoolYearRepo;
    private final SchoolRepository schoolRepo;
    private final StaffRepository staffRepo;
    public ClassRoomService(ClassRoomRepository repo, GradeLevelRepository gradeLevelRepo, SchoolYearRepository schoolYearRepo, SchoolRepository schoolRepo, StaffRepository staffRepo) {
        this.repo = repo; this.gradeLevelRepo = gradeLevelRepo; this.schoolYearRepo = schoolYearRepo; this.schoolRepo = schoolRepo; this.staffRepo = staffRepo;
    }

    private huy.example.demoMonday.dto.response.ClassRoomResp toDto(ClassRoom e){
        var b = huy.example.demoMonday.dto.response.ClassRoomResp.builder().id(e.getId());
        b.name(e.getName()).gradeLevelId(e.getGradeLevel().getId()).schoolYearId(e.getSchoolYear().getId()).schoolId(e.getSchool().getId());
        b.homeroomTeacherId(e.getHomeroomTeacher()==null?null:e.getHomeroomTeacher().getId());
        return b.build();
    }

    @Transactional
    public huy.example.demoMonday.dto.response.ClassRoomResp create(huy.example.demoMonday.dto.request.ClassRoomReq req){
        var e = new ClassRoom();
        e.setName(req.getName());
        e.setGradeLevel(gradeLevelRepo.findById(req.getGradeLevelId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("GradeLevel not found: "+req.getGradeLevelId())));
        e.setSchoolYear(schoolYearRepo.findById(req.getSchoolYearId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("SchoolYear not found: "+req.getSchoolYearId())));
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        e.setHomeroomTeacher(req.getHomeroomTeacherId()==null?null:staffRepo.findById(req.getHomeroomTeacherId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Staff not found: "+req.getHomeroomTeacherId())));
        return toDto(repo.save(e));
    }

    @Transactional
    public huy.example.demoMonday.dto.response.ClassRoomResp update(java.util.UUID id, huy.example.demoMonday.dto.request.ClassRoomReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("ClassRoom not found: " + id));
        e.setName(req.getName());
        e.setGradeLevel(gradeLevelRepo.findById(req.getGradeLevelId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("GradeLevel not found: "+req.getGradeLevelId())));
        e.setSchoolYear(schoolYearRepo.findById(req.getSchoolYearId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("SchoolYear not found: "+req.getSchoolYearId())));
        e.setSchool(schoolRepo.findById(req.getSchoolId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("School not found: "+req.getSchoolId())));
        e.setHomeroomTeacher(req.getHomeroomTeacherId()==null?null:staffRepo.findById(req.getHomeroomTeacherId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Staff not found: "+req.getHomeroomTeacherId())));
        return toDto(repo.save(e));
    }

    public huy.example.demoMonday.dto.response.ClassRoomResp get(java.util.UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("ClassRoom not found: " + id));
        return toDto(e);
    }

    public java.util.List<huy.example.demoMonday.dto.response.ClassRoomResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<huy.example.demoMonday.dto.response.ClassRoomResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(java.util.UUID id){ repo.deleteById(id); }
}

