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
import java.util.ArrayList;
import java.util.HashMap;


@Controller
@RequiredArgsConstructor
public class SummonerCopy {

    private final SummonerService summonerService;


    // 소환사 Summoner(smn) 검색
    @GetMapping("/summoner")
    public String summoner(@RequestParam("smn") String smn, SummonerDto
            summonerDto, Model model) throws ParseException, IOException {

        smn.replaceAll(" ", "");//검색 명 공백제거
        model.addAttribute("smn", smn);

        summonerService.smnSearch(summonerDto, smn);

        // 임시
        model.addAttribute("summonerDto", summonerDto);
        return "summoner/smnResult";
    }
}
