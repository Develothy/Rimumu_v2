package gg.rimumu.service;

import gg.rimumu.domain.entity.Member;
import gg.rimumu.domain.repository.MemberRepository;
import gg.rimumu.dto.MemberDto;
import gg.rimumu.exception.RimumuException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public String createMember(MemberDto memberDto) {

        memberRepository.findByEmail(memberDto.getEmail()).ifPresent( member -> {
                    new RimumuException.MemberAlreadyRegisteredException(memberDto.getEmail());
        });

        return memberRepository.save(Member.of(memberDto)).getEmail();
    }

}
