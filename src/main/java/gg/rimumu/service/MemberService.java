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

    public String login(String email, String password) throws RimumuException.MemberValidationException {

        Member member = memberRepository.findByEmail(email).orElseThrow(() ->
                    new RimumuException.MemberValidationException());

        if (!password.equals(member.getPassword())) {
            throw new RimumuException.MemberValidationException();
        }

        return "로그인 성공";
    }

}
