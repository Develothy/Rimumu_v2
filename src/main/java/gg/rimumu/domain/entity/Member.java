package gg.rimumu.domain.entity;


import gg.rimumu.dto.MemberDto;
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

    public static Member of(MemberDto memberDto) {
        return Member.builder()
                .email(memberDto.getEmail())
                .password(memberDto.getPassword())
                .role(Role.MEMBER)
                .build();
    }


}
