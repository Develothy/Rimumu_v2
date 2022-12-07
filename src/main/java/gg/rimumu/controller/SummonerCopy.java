package gg.rimumu.controller;

import gg.rimumu.dto.SummonerDto;
import gg.rimumu.service.SummonerService;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;


@Controller
@RequiredArgsConstructor
public class SummonerCopy {

    private final SummonerService summonerService;


    // 소환사 Summoner(smn) 검색
    @GetMapping("/summoner")
    public String summoner(@RequestParam("smn") String smn, SummonerDto summonerDto, Model model) throws ParseException, IOException {

        smn = smn.trim();
        if (smn.length() < 3){ // 2글자 소환사는 가운데에 공백 꼭 필요
            smn = smn.substring(0,1) + " " + smn.substring(1);
        }
        smn = URLEncoder.encode(smn, "utf-8");

        summonerDto = summonerService.smnSearch(summonerDto, smn);

        model.addAttribute("summonerDto", summonerDto);
        return "summoner/smnResult";
    }
}
