package gg.rimumu.controller;

import gg.rimumu.common.RimumuResult;
import gg.rimumu.dto.Match;
import gg.rimumu.dto.Summoner;
import gg.rimumu.exception.RimumuException;
import gg.rimumu.service.SummonerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        System.out.println("====controller summoner==== :::" + adjustSmn);

        try {
            Summoner summoner = summonerService.smnSearch(URLEncoder.encode(adjustSmn, StandardCharsets.UTF_8));
            return new RimumuResult(summoner);

        } catch (RimumuException e) {
            return new RimumuResult<>(e.code, e.getMessage(), null);
        }
    }

    @GetMapping("/matches")
    @ResponseBody
    public RimumuResult matches (@RequestParam(required = false) String smnPuuid,
                                 @RequestParam(required = false) String smn,
                                 @RequestParam(required = false, defaultValue = "0") int offset) {

        try {
            if (smnPuuid == null) {
                String adjustSmn = smn.strip().length() > 2 ? smn : smn.charAt(0) + " " + smn.charAt(1);
                smnPuuid = summonerService.getSmnPuuid(URLEncoder.encode(adjustSmn, StandardCharsets.UTF_8));
            }
            System.out.println("====controller matches====");
            List<Match> matches = summonerService.getMatches(smnPuuid, offset);

            RimumuResult result = new RimumuResult<>(matches);
            System.out.println("result" + result.getData());
            return result;

        } catch (RimumuException e) {
            return new RimumuResult<>(e.code, e.getMessage(), null);
        }
    }
}
