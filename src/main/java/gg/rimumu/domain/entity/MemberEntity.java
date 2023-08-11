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
public class MemberEntity {

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

    public static MemberEntity of(gg.rimumu.dto.Member member) {
        return MemberEntity.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .role(Role.MEMBER)
                .build();
    }


}
