package huy.example.demoMonday.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    private String fullName;

    private java.time.LocalDate dob;

    @Enumerated(EnumType.STRING)
    private huy.example.demoMonday.enums.Gender gender;

    @Column(unique = true)
    private String studentCode;

    @ManyToOne
    private ClassRoom currentClass;

    @ManyToOne(optional = false)
    private School school;

    private String photoUrl;

    @Enumerated(EnumType.STRING)
    private huy.example.demoMonday.enums.StudentStatus status;

    // >>> thêm quan hệ tài khoản để PostDbBootstrap dùng được
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private UserAccount user;
}
