package gg.rimumu.controller;

import gg.rimumu.common.result.RimumuResult;
import gg.rimumu.dto.Member;
import gg.rimumu.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/login")
    public String login(Member member) {

        memberService.login(member);
        return "";
    }

    //http://localhost:8088/login/callback
    @GetMapping("/login/callback")
    public ResponseEntity<RimumuResult> callback(HttpServletRequest req) {

        String token = memberService.getSocialInfo(req);
        return ResponseEntity.ok()
                .body(new RimumuResult<>(token));
    }
}
