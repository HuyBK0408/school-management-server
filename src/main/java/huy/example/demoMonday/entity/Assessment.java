package huy.example.demoMonday.entity;

import huy.example.demoMonday.enums.AssessmentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "assessment",
        indexes = {
                @Index(name = "ix_assessment_group",
                        columnList = "subject_id, class_room_id, term_id")
        })
@Getter @Setter
@NoArgsConstructor
public class Assessment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    //@Column(nullable = false, length = 150)
    //private String title;

    @Column(nullable = false)
    private Integer weight = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AssessmentType type;

    // ==== map quan hệ, trỏ vào đúng cột FK hiện có ====
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_room_id", nullable = false)
    private ClassRoom classRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @Column(length = 1000)
    private String description;

    private LocalDate examDate;

    // ==== tiện ích: nếu bạn vẫn cần lấy nhanh ID ====
    public UUID getClassRoomId() { return classRoom != null ? classRoom.getId() : null; }
    public UUID getSubjectId()   { return subject   != null ? subject.getId()   : null; }
    public UUID getTermId()      { return term      != null ? term.getId()      : null; }
}
