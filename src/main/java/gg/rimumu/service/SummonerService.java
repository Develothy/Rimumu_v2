package gg.rimumu.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class SummonerService {

    @Value("${LoL_KEY}")
    private String API_KEY;

    @Value("${DDUrl}")
    private String ddUrl;

    @Value("${DDVer}")
    private String ddVer;

    final JSONParser jsonParser = new JSONParser();


    // api 연결
    public String urlConn(String urlConn) throws IOException {

        String apiResult = "";

        URL url = new URL(urlConn);
        BufferedReader bf; //buffer reread 전송 전 임시 보관소(입출력 속도향상)
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        bf = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
        System.out.println(bf);

        apiResult = bf.readLine();

        return apiResult;
    }

    // 소환사 검색
    public JSONObject smnSearch(String smn) throws IOException, ParseException {
        String url = "https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-name/" + smn + "?api_key=" + API_KEY;

        String result = urlConn(url);

        // 존재하는 소환사
        //검색 소환사 account 정보 가져오기
        JSONObject accResult = (JSONObject) jsonParser.parse(result);
        System.out.println("Service _ String 타입 : " + result);
        System.out.println("Service _ Json 타입 : " + accResult);

        return accResult;
    }

    //소환사 정보
    public String smnInfo(JSONObject accResult) throws IOException, ParseException {

        //result를 Json화 해준 후 이름, 아이콘, 고유id 가져오기
        String name = accResult.get("name").toString();
        int smLv = Integer.parseInt(accResult.get("summonerLevel").toString());
        int profileIconId = Integer.parseInt(accResult.get("profileIconId").toString());
        String imgIconUrl = ddUrl + ddVer + "/img/profileicon/" + profileIconId + ".png";

        String id = accResult.get("id").toString();
        String puuid = accResult.get("puuid").toString();


        // 티어 조회
        String rankUrl = "https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/" + id + "?api_key=" + API_KEY;




        return "name, smLv, imgIconUrl";
    }


    // 티어 구하기
    // 티어 구하기
    public String smnTier(String rankUrl) throws IOException, ParseException {
        String rankResult = urlConn(rankUrl);
        //언랭일 경우 [] 값 null
        if ("[]".equals(rankResult)) {
            /*model.addAttribute("soloTier", "Unranked");
            model.addAttribute("flexTier", "Unranked");*/
        } // 언랭 아닐 경우 [] 값 확인
        else {
            JSONArray json_rank_result = (JSONArray) jsonParser.parse(rankResult);
            //솔랭, 자랭 구분하기
            for (int i = 0; i < json_rank_result.size(); i++) {
                JSONObject obj_rank_result = (JSONObject) json_rank_result.get(i);
                String rankType = obj_rank_result.get("queueType").toString();

                // 솔랭, 자랭 값이 존재 한다면 해당 tier값으로 덮음

                //솔랭일 경우
                if ("RANKED_SOLO_5x5".equals(rankType)) {
                    String tier = obj_rank_result.get("tier").toString(); // 챌, 다이아, 플레 등
                    String rank = obj_rank_result.get("rank").toString();  // 1 ~ 4
                    String leaguePoints = obj_rank_result.get("leaguePoints").toString(); // 티어 LP
                    String wins = obj_rank_result.get("wins").toString(); //랭크 전체 승
                    String losses = obj_rank_result.get("losses").toString(); //랭크 전체 패

                    System.out.print(obj_rank_result);
                    System.out.println("솔랭 : " + tier + rank);


                } //자랭일 경우
                if ("RANKED_FLEX_SR".equals(rankType)) {
                    String tier = obj_rank_result.get("tier").toString();
                    String rank = obj_rank_result.get("rank").toString();
                    String leaguePoints = obj_rank_result.get("leaguePoints").toString();
                    String wins = obj_rank_result.get("wins").toString();
                    String losses = obj_rank_result.get("losses").toString();

                    System.out.print(obj_rank_result);
                    System.out.println("자랭 : " + tier + rank);

                }
            }
        }
        return "";
    }



}
