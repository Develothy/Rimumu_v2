package gg.rimumu.controller;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
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
        JSONObject accResult = (JSONObject) jsonParser.parse(apiResult);

        System.out.println("String 타입 : "+ apiResult);
        System.out.println("Json 타입 : " + accResult);

        //APIresult를 Json화 해준 후 이름, 아이콘, 고유id 가져오기
        String json_name = accResult.get("name").toString();
        int profileIconId  = Integer.parseInt(accResult.get("profileIconId").toString());
        String id  = accResult.get("id").toString();
        String puuid  = accResult.get("puuid").toString();
        int smLv = Integer.parseInt(accResult.get("summonerLevel").toString());

        // 데이터 jsp로 넘기기
        model.addAttribute("name", json_name);
        model.addAttribute("smLv",smLv);
        model.addAttribute("imgIconURL", ddUrl +ddVer+"/img/profileicon/"+profileIconId+".png");



        // 검색 소환사 랭크 불러오기
        String rankResult ="";
        try {
            String rankUrl = "https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/"+id+"?api_key="+API_KEY;
            URL url = new URL(rankUrl);

            BufferedReader bf;
            HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
            urlconnection.setRequestMethod("GET");
            bf = new BufferedReader(new InputStreamReader(urlconnection.getInputStream(), "UTF-8"));
            System.out.println(bf);

            rankResult = bf.readLine();
        }catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("String 타입 : "+ rankResult);

        //언랭일 경우 [] 값 null
        if("[]".equals(rankResult)) {
            model.addAttribute("soloTier", "Unranked");
            model.addAttribute("flexTier", "Unranked");
        } // 언랭 아닐 경우 [] 값 확인
        else{
            JSONArray json_rank_result =(JSONArray) jsonParser.parse(rankResult);
            //솔랭, 자랭 구분하기
            for(int i = 0; i < json_rank_result.size(); i++){
                JSONObject obj_rank_result = (JSONObject) json_rank_result.get(i);
                String rankType = obj_rank_result.get("queueType").toString();

                // 솔랭, 자랭 값이 존재 한다면 해당 tier값으로 덮음
                model.addAttribute("soloTier", "Unranked");
                model.addAttribute("flexTier", "Unranked");

                //솔랭일 경우
                if("RANKED_SOLO_5x5".equals(rankType)){
                    String tier = obj_rank_result.get("tier").toString(); // 챌, 다이아, 플레 등
                    String rank = obj_rank_result.get("rank").toString();  // 1 ~ 4
                    String leaguePoints = obj_rank_result.get("leaguePoints").toString(); // 티어 LP
                    String wins = obj_rank_result.get("wins").toString(); //랭크 전체 승
                    String losses = obj_rank_result.get("losses").toString(); //랭크 전체 패

                    System.out.print(obj_rank_result);
                    System.out.println("솔랭 : "+ tier + rank);

                    //view로 값 넘겨주기 (String 값)
                    model.addAttribute("soloTier", tier);
                    model.addAttribute("soloRank", rank);
                    model.addAttribute("soloLeaguePoints", leaguePoints+" LP");
                    model.addAttribute("soloWins", wins+" 승");
                    model.addAttribute("soloLosses", losses+" 패");
                } //자랭일 경우

                if("RANKED_FLEX_SR".equals(rankType)){
                    String tier = obj_rank_result.get("tier").toString();
                    String rank = obj_rank_result.get("rank").toString();
                    String leaguePoints = obj_rank_result.get("leaguePoints").toString();
                    String wins = obj_rank_result.get("wins").toString();
                    String losses = obj_rank_result.get("losses").toString();

                    System.out.print(obj_rank_result);
                    System.out.println("자랭 : "+ tier + rank);

                    model.addAttribute("flexTier", tier);
                    model.addAttribute("flexRank", rank);
                    model.addAttribute("flexLeaguePoints", leaguePoints+" LP");
                    model.addAttribute("flexWins", wins+" 승");
                    model.addAttribute("flexLosses", losses+" 패");
                }
            }
        }

        // 인게임 정보 current
        //https://kr.api.riotgames.com/lol/spectator/v4/active-games/by-summoner/x2OV0C24um6oOgMaj-jhhpDO1WAlCaH_yqyYLf6SQxIY4g?api_key=RGAPI-2e5cc780-361d-4e27-b253-c0823e44289e
        String curResult ="";
        try {
            String curUrl = "https://kr.api.riotgames.com/lol/spectator/v4/active-games/by-summoner/"+id+"?api_key="+API_KEY;
            URL url = new URL(curUrl);

            BufferedReader bf;
            HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
            urlconnection.setRequestMethod("GET");
            bf = new BufferedReader(new InputStreamReader(urlconnection.getInputStream(), "UTF-8"));
            System.out.println(bf);

            curResult = bf.readLine();
        }catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("String 타입 : "+ curResult);

        try {

            JSONObject curData = (JSONObject) jsonParser.parse(curResult);

            //curResult를 JSON화 해준 후 인게임 정보 가져오기

            String queueId = curData.get("gameQueueConfigId").toString();

            System.out.println("현재 게임 정보 current"+queueId);

            switch (queueId) {
                case "420" : queueId="솔랭";
                    break;
                case "430" : queueId="일반";
                    break;
                case "440" : queueId="자유랭크";
                    break;
                case "450" : queueId="칼바람";
                    break;
                case "900" : queueId="우르프";
                    break;
            }
            model.addAttribute("current", "yes");
            model.addAttribute("queueId", queueId);

            // participants : xx 부분
            JSONArray partiInGame = (JSONArray) curData.get("participants");

            for(int p = 0; p < partiInGame.size(); p++){
                System.out.println("플레이어 포문 진입, p = " + p + "/" + partiInGame.size());

                JSONArray inGameArr = (JSONArray) partiInGame.get(p);

                // JSON Array -> JSON Object
                JSONObject inGame = new JSONObject();
                if (inGameArr.size()>0){
                    for (int i=0;i<inGameArr.size(); i++){
                        inGame = (JSONObject) inGameArr.get(i);
                    }
                }

                //key값 증가. 소환사 이름 sm0.sm1.sm2...값 차례대로 넣어주기
                String sm = "sm"+ p;
                //<img SRC="smImgUrl">
                String smImgUrl = "smImgUrl"+p;

                //inGame summoner(p)의 소환사 명
                String inName = (String) inGame.get("summonerName");
                System.out.println(sm+inName);


                try {
                    //inGame summoner(p)의 챔피언 key값
                    String inChampId = inGame.get("championId").toString();
                    String getKey = "K"+inChampId;



                    model.addAttribute(sm, inName);
                    //	model.addAttribute(smImgUrl, ddUrl + ddVer + "/img/champion/"+inChamp+".png");

                } catch (Exception e) {
                    System.out.println("------Dto오류-------------");
                }
            }
            // 게임 중 아님 current 404
        }catch (Exception e) {
            model.addAttribute("current", "404");

        }


        // 검색한 소환사 최근 전적 승률 KDA
        int recentWin=0;
        int recentLose=0;
        int recentKill=0;
        int recentDeath=0;
        int recentAssist=0;

        // 매치 리스트 가져오기 matchId
        String matchIdResult = null;
        try {
            String match_urls = "https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/"+puuid+"/ids?start=0&count20&api_key="+ API_KEY;
            System.out.println(match_urls);

            URL url = new URL(match_urls);

            BufferedReader bf;
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            bf = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            System.out.println(bf);

            matchIdResult = bf.readLine();
        }catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("String 타입 : "+ matchIdResult);




        return "summoner/smnResult";
    }


}
