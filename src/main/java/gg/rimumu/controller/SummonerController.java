package gg.rimumu.controller;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



@Controller
@RequiredArgsConstructor
public class SummonerController {

    final static String API_KEY = "RGAPI-032475c9-844d-4beb-82f9-2a1132ee2666";
    String ddUrl = "https://ddragon.leagueoflegends.com/cdn/"; //Data dragon 링크 (이미지 정보)
    String ddVer = "12.14.1"; //Data dragon version

    // 소환사 Summoner(smn) 검색
    @GetMapping("/summoner")
    public String summoner(@RequestParam("smn") String smn, HttpServletRequest httpServletRequest, Model model) throws ParseException {

        smn.replaceAll(" ","");//검색 명 공백제거
        model.addAttribute("smn", smn);

        // smn URL 연결
        String apiResult ="";
        try{
            String smnUrl = "https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-name/"+smn+"?api_key="+API_KEY;
            URL url = new URL(smnUrl);
            BufferedReader bf; //buffer reread 전송 전 임시 보관소(입출력 속도향상)
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            bf = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            System.out.println(bf);

            apiResult = bf.readLine();

        }catch(Exception e){
            // 존재하지 않는 소환사
            model.addAttribute("smn", smn);
            return "summoner/nameNull";
        }

        // 존재하는 소환사
        JSONParser jsonParser = new JSONParser();
        //검색 소환사 account 정보 가져오기
        JSONObject jsonObject = new JSONObject();
        JSONObject accResult = (JSONObject) jsonParser.parse(apiResult);

        System.out.println("String 타입 : "+ apiResult);
        System.out.println("Json 타입 : " + accResult);

        //APIresult를 Json화 해준 후 이름, 아이콘, 고유id 가져오기
        String json_name = accResult.get("name").toString();
        int profileIconId  = Integer.parseInt(accResult.get("profileIconId").toString());
        String id  = accResult.get("id").toString();
        String puuid  = accResult.get("puuid").toString();
        int smLv = Integer.parseInt(accResult.get("summonerLevel").toString());











        return "summoner/smnResult";
    }


}
