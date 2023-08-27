package gg.rimumu.controller;

import gg.rimumu.dto.Member;
import gg.rimumu.exception.RimumuException;
import gg.rimumu.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public String create(@RequestBody Member member) {

        if (ObjectUtils.isEmpty(member)) {
            return "회원가입 정보를 정확히 입력해주세요!";
        }

        try {
            return memberService.createMember(member);
        } catch (Exception e) {
            return "로그인에 실패하였습니다." + e.getMessage();
        }

    }

    @PostMapping("/login")
    public String login(@RequestBody Member member) {

        String email = member.getEmail();
        String password = member.getPassword();

        if ( email.isBlank() || password.isBlank()) {
            return "아이디 또는 비밀번호가 입력되지 않았습니다.";
        }
        try {
            return memberService.login(email, password);
        } catch (Exception e) {
            return "로그인에 실패하였습니다." + e.getMessage();
        }
    }

}
