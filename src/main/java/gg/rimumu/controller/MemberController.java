package gg.rimumu.controller;

import gg.rimumu.dto.MemberDto;
import gg.rimumu.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public String create(@RequestBody MemberDto memberDto) {
        return memberService.createMember(memberDto);
    }

}
