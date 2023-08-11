package gg.rimumu.service;

import gg.rimumu.domain.entity.MemberEntity;
import gg.rimumu.domain.repository.MemberRepository;
import gg.rimumu.dto.Member;
import gg.rimumu.exception.RimumuException;
import gg.rimumu.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final EncryptUtil encryptUtil;

    public String createMember(Member member) throws RimumuException {

        memberRepository.findByEmail(member.getEmail()).ifPresent( it ->
                    new RimumuException.MemberAlreadyRegisteredException(member.getEmail())
        );

        try {
            member.setPassword(encryptUtil.encrypt(member.getPassword()));
            return memberRepository.save(MemberEntity.of(member)).getEmail();

        } catch (RimumuException.EncryptException e) {
            throw new RimumuException.EncryptException(member.getPassword());
        } catch (Exception e) {
            throw new RimumuException.MemberValidationException();
        }

    }

    public String login(String email, String password) throws RimumuException {

        MemberEntity member = memberRepository.findByEmail(email).orElseThrow(() ->
                    new RimumuException.MemberValidationException());

        if (!password.equals(member.getPassword())) {
            throw new RimumuException.MemberValidationException();
        }

        return "로그인 성공";
    }

}
