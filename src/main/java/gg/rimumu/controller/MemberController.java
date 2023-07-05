package gg.rimumu.controller;

import gg.rimumu.dto.MemberDto;
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
    public String create(@RequestBody MemberDto memberDto) {

        if (ObjectUtils.isEmpty(memberDto)) {
            return "회원가입 정보를 정확히 입력해주세요!";
        }
        return memberService.createMember(memberDto);
    }

}
