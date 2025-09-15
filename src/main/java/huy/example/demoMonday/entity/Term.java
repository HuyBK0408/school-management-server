package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "term",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_term_order", columnNames = {"school_year_id", "order_no"}),
                @UniqueConstraint(name = "uq_term_name",  columnNames = {"school_year_id", "name"})
        })
@Getter @Setter
public class Term extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "order_no", nullable = false)
    private Integer orderNo;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "school_year_id", nullable = false)
    private SchoolYear schoolYear;


}
