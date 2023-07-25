package gg.rimumu.controller;

import gg.rimumu.dto.Summoner;
import gg.rimumu.exception.RimumuException;
import gg.rimumu.service.SummonerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Controller
@RequiredArgsConstructor
public class SummonerCopy {

    private final SummonerService summonerService;


    // 소환사 Summoner(smn) 검색
    @GetMapping("/summoner")
    public String summoner(@RequestParam("smn") String smn,
                           @RequestParam(required = false, defaultValue = "0") int offset,
                           Model model) throws IOException {
        if (smn.isBlank()) {
            return "summoner/nameNull";
        }

        String adjustSmn = smn.strip().length() > 2 ? smn : smn.charAt(0) + " " + smn.charAt(1);

        Summoner summoner;
        try {
            summoner = summonerService.smnSearch(URLEncoder.encode(adjustSmn, StandardCharsets.UTF_8), offset);
        } catch (RimumuException.SummonerNotFoundException e) {
            model.addAttribute("smn", smn);
            return "summoner/nameNull";
        } catch (RimumuException e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("summoner", summoner);
        return "summoner/smnResult";
    }
}
