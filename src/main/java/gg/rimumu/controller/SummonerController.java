package gg.rimumu.controller;

import gg.rimumu.common.RimumuResult;
import gg.rimumu.dto.Match;
import gg.rimumu.dto.Summoner;
import gg.rimumu.exception.RimumuException;
import gg.rimumu.service.SummonerService;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
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
    public RimumuResult info (@RequestParam String smn,
                                      @RequestParam(required = false, defaultValue = "0") int offset) throws RimumuException {

        String adjustSmn = smn.strip().length() > 2 ? smn : smn.charAt(0) + " " + smn.charAt(1);

        Summoner summoner = summonerService.smnSearch(URLEncoder.encode(adjustSmn, StandardCharsets.UTF_8), offset);

        System.out.println(summoner);
        return new RimumuResult(summoner);
    }

    @GetMapping("/matches")
    @ResponseBody
    public List<Match> matches (@RequestParam String userPuuid,
                                    @RequestParam(required = false, defaultValue = "0") int offset) {

        List<Match> matches = new ArrayList<>();

        matches = summonerService.smnMatches(userPuuid, offset);

        System.out.println(matches.size());
        return matches;
    }
}
