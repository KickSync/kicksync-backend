package be.kicksync_backend.feature.user.entity;

import be.kicksync_backend.common.entity.BaseTimeEntity;
import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, length = 250)
    private String password;

    @Column(unique = true, length = 20)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Payment> payments = new ArrayList<>();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.role = Role.USER;
    }

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public void updateNickname(String nickname) {
        if (nickname == null) {
            this.nickname = null;
            return;
        }
        String v = nickname.strip();
        if (v.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME);
        }
        this.nickname = v;
    }
}
