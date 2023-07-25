package gg.rimumu.domain.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    enum Role {
        GEUST, MEMBER, ADMIN
    }

    public static Member of(gg.rimumu.dto.Member member) {
        return gg.rimumu.domain.entity.Member.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .role(Role.MEMBER)
                .build();
    }


}
