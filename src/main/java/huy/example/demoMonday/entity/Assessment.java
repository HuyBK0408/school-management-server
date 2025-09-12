package huy.example.demoMonday.entity;

import huy.example.demoMonday.enums.AssessmentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "assessment")
@Getter @Setter
@NoArgsConstructor
public class Assessment extends BaseEntity {
    @Id @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private AssessmentType assessmentType;

    private Integer weight;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "class_id")
    private ClassRoom classRoom;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "term_id")
    private Term term;

    private LocalDate examDate;
}
