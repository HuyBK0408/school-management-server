package huy.example.demoMonday.service;

import huy.example.demoMonday.dto.request.TermReq;
import huy.example.demoMonday.dto.response.TermResp;
import huy.example.demoMonday.entity.Term;
import huy.example.demoMonday.repository.SchoolYearRepository;
import huy.example.demoMonday.repository.TermRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TermService {
    private final TermRepository repo;
    private final SchoolYearRepository schoolYearRepo;

    @PersistenceContext
    private EntityManager em;

    public TermService(TermRepository repo, SchoolYearRepository schoolYearRepo) {
        this.repo = repo;
        this.schoolYearRepo = schoolYearRepo;
    }

    private TermResp toDto(Term e){
        return TermResp.builder()
                .id(e.getId())
                .name(e.getName())
                .orderNo(e.getOrderNo())
                .schoolYearId(e.getSchoolYear().getId())
                .build();
    }

    @Transactional
    public TermResp create(TermReq req){
        var sy = schoolYearRepo.findById(req.getSchoolYearId())
                .orElseThrow(() -> new EntityNotFoundException("SchoolYear not found: " + req.getSchoolYearId()));

        // Validate orderNo 1..3
        if (req.getOrderNo() == null || req.getOrderNo() < 1 || req.getOrderNo() > 3) {
            throw new IllegalArgumentException("orderNo must be between 1 and 3");
        }

        // Max 3 terms / schoolYear
        long totalInYear = em.createQuery(
                        "select count(t) from Term t where t.schoolYear.id = :syId", Long.class)
                .setParameter("syId", sy.getId())
                .getSingleResult();
        if (totalInYear >= 3) {
            throw new IllegalStateException("A school_year can only have at most 3 terms");
        }

        // No duplicate orderNo in same schoolYear
        long dupOrder = em.createQuery(
                        "select count(t) from Term t where t.schoolYear.id = :syId and t.orderNo = :ord", Long.class)
                .setParameter("syId", sy.getId())
                .setParameter("ord", req.getOrderNo())
                .getSingleResult();
        if (dupOrder > 0) {
            throw new IllegalStateException("order_no already exists in this school_year");
        }

        // No duplicate name in same schoolYear
        long dupName = em.createQuery(
                        "select count(t) from Term t where t.schoolYear.id = :syId and lower(t.name) = lower(:name)", Long.class)
                .setParameter("syId", sy.getId())
                .setParameter("name", req.getName())
                .getSingleResult();
        if (dupName > 0) {
            throw new IllegalStateException("term name already exists in this school_year");
        }

        var e = new Term();
        e.setName(req.getName());
        e.setOrderNo(req.getOrderNo());
        e.setSchoolYear(sy);

        return toDto(repo.save(e));
    }

    @Transactional
    public TermResp update(UUID id, TermReq req){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Term not found: " + id));

        var sy = schoolYearRepo.findById(req.getSchoolYearId())
                .orElseThrow(() -> new EntityNotFoundException("SchoolYear not found: " + req.getSchoolYearId()));

        if (req.getOrderNo() == null || req.getOrderNo() < 1 || req.getOrderNo() > 3) {
            throw new IllegalArgumentException("orderNo must be between 1 and 3");
        }

        // Nếu đổi schoolYear, đảm bảo tổng không vượt 3
        if (!e.getSchoolYear().getId().equals(sy.getId())) {
            long totalInYear = em.createQuery(
                            "select count(t) from Term t where t.schoolYear.id = :syId", Long.class)
                    .setParameter("syId", sy.getId())
                    .getSingleResult();
            if (totalInYear >= 3) {
                throw new IllegalStateException("A school_year can only have at most 3 terms");
            }
        }

        // Không trùng order trong schoolYear (loại trừ chính record đang sửa)
        long dupOrder = em.createQuery(
                        "select count(t) from Term t where t.schoolYear.id = :syId and t.orderNo = :ord and t.id <> :id",
                        Long.class)
                .setParameter("syId", sy.getId())
                .setParameter("ord", req.getOrderNo())
                .setParameter("id", e.getId())
                .getSingleResult();
        if (dupOrder > 0) {
            throw new IllegalStateException("order_no already exists in this school_year");
        }

        // Không trùng name trong schoolYear (loại trừ chính record đang sửa)
        long dupName = em.createQuery(
                        "select count(t) from Term t where t.schoolYear.id = :syId and lower(t.name) = lower(:name) and t.id <> :id",
                        Long.class)
                .setParameter("syId", sy.getId())
                .setParameter("name", req.getName())
                .setParameter("id", e.getId())
                .getSingleResult();
        if (dupName > 0) {
            throw new IllegalStateException("term name already exists in this school_year");
        }

        e.setName(req.getName());
        e.setOrderNo(req.getOrderNo());
        e.setSchoolYear(sy);

        return toDto(repo.save(e));
    }

    public TermResp get(UUID id){
        var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Term not found: " + id));
        return toDto(e);
    }

    public java.util.List<TermResp> list(){
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public org.springframework.data.domain.Page<TermResp> page(org.springframework.data.domain.Pageable pageable){
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void delete(UUID id){ repo.deleteById(id); }
}
