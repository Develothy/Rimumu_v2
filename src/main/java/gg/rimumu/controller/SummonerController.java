package gg.rimumu.controller;

import gg.rimumu.common.RimumuResult;
import gg.rimumu.dto.Match;
import gg.rimumu.dto.Summoner;
import gg.rimumu.service.SummonerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class SummonerController extends BaseController {

    private final SummonerService summonerService;

    // 소환사 Summoner(smn) 검색
    @GetMapping("/summoner")
    @ResponseBody
    public RimumuResult info (@RequestParam String smn) {
        // 2글자 닉네임 버그 조정
        String adjustSmn = smn.strip().length() > 2 ? smn : smn.charAt(0) + " " + smn.charAt(1);

        try {
            Summoner summoner = summonerService.smnSearch(URLEncoder.encode(adjustSmn, StandardCharsets.UTF_8));
            System.out.println(summoner);
            return new RimumuResult(summoner);

        } catch (Exception e) {
            return new RimumuResult<>(500, e.getMessage(), null);
        }
    }

    @GetMapping("/matches")
    @ResponseBody
    public RimumuResult matches (@RequestParam String userPuuid,
                                    @RequestParam(required = false, defaultValue = "0") int offset) {

        try {
            List<Match> matches = summonerService.smnMatches(userPuuid, offset);
            System.out.println(matches.size());
            return new RimumuResult<>(matches);

        } catch (Exception e) {
            return new RimumuResult<>(500, e.getMessage(), null);
        }
    }
}
