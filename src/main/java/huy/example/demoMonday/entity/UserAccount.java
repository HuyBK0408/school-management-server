package huy.example.demoMonday.entity;

import huy.example.demoMonday.enums.SoftStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class UserAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    // Đủ dài cho {id} + hash (bcrypt/argon2/scrypt)
    @Column(nullable = false, length = 255)
    private String passwordHash;

    // Salt riêng cho user (Base64). 32 bytes → ~44 ký tự Base64.
    @Column(nullable = false, length = 44)
    private String passwordSalt;

    @Column(length = 30)
    private String phone;

    @ManyToOne
    private School school;

    @Column(nullable = false)
    private boolean enabled = true; // sẽ set false khi mới tạo và chờ verify

    @Enumerated(EnumType.STRING)
    @Column(name = "soft_status", nullable = false)
    private SoftStatus softStatus = SoftStatus.ACTIVE;
}
