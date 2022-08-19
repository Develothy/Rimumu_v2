/*
package gg.rimumu.controller;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


@Controller
@RequiredArgsConstructor
public class SummonerController {


    // 소환사 Summoner(smn) 검색
    @GetMapping("/summoner")
    public String summoner(@RequestParam("smn") String smn, HttpServletRequest httpServletRequest, Model model) throws ParseException, IOException {

        smn.replaceAll(" ", "");//검색 명 공백제거
        model.addAttribute("smn", smn);

        // smn URL 연결
        String apiResult = "";
        try {
            String smnUrl = "https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-name/" + smn + "?api_key=" + API_KEY;
            URL url = new URL(smnUrl);
            BufferedReader bf; //buffer reread 전송 전 임시 보관소(입출력 속도향상)
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            bf = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            System.out.println(bf);

            apiResult = bf.readLine();

        } catch (Exception e) {
            // 존재하지 않는 소환사
            model.addAttribute("smn", smn);
            return "summoner/nameNull";
        }

        // 존재하는 소환사
        JSONParser jsonParser = new JSONParser();
        //검색 소환사 account 정보 가져오기
        JSONObject accResult = (JSONObject) jsonParser.parse(apiResult);

        System.out.println("String 타입 : " + apiResult);
        System.out.println("Json 타입 : " + accResult);

        //APIresult를 Json화 해준 후 이름, 아이콘, 고유id 가져오기
        String json_name = accResult.get("name").toString();
        int profileIconId = Integer.parseInt(accResult.get("profileIconId").toString());
        String id = accResult.get("id").toString();
        String puuid = accResult.get("puuid").toString();
        int smLv = Integer.parseInt(accResult.get("summonerLevel").toString());

        // 데이터 jsp로 넘기기
        model.addAttribute("name", json_name);
        model.addAttribute("smLv", smLv);
        model.addAttribute("imgIconURL", ddUrl + ddVer + "/img/profileicon/" + profileIconId + ".png");


        // 검색 소환사 랭크 불러오기
        String rankResult = "";
        try {
            String rankUrl = "https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/" + id + "?api_key=" + API_KEY;
            URL url = new URL(rankUrl);

            BufferedReader bf;
            HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
            urlconnection.setRequestMethod("GET");
            bf = new BufferedReader(new InputStreamReader(urlconnection.getInputStream(), "UTF-8"));
            System.out.println(bf);

            rankResult = bf.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("String 타입 : " + rankResult);

        //언랭일 경우 [] 값 null
        if ("[]".equals(rankResult)) {
            model.addAttribute("soloTier", "Unranked");
            model.addAttribute("flexTier", "Unranked");
        } // 언랭 아닐 경우 [] 값 확인
        else {
            JSONArray json_rank_result = (JSONArray) jsonParser.parse(rankResult);
            //솔랭, 자랭 구분하기
            for (int i = 0; i < json_rank_result.size(); i++) {
                JSONObject obj_rank_result = (JSONObject) json_rank_result.get(i);
                String rankType = obj_rank_result.get("queueType").toString();

                // 솔랭, 자랭 값이 존재 한다면 해당 tier값으로 덮음
                model.addAttribute("soloTier", "Unranked");
                model.addAttribute("flexTier", "Unranked");

                //솔랭일 경우
                if ("RANKED_SOLO_5x5".equals(rankType)) {
                    String tier = obj_rank_result.get("tier").toString(); // 챌, 다이아, 플레 등
                    String rank = obj_rank_result.get("rank").toString();  // 1 ~ 4
                    String leaguePoints = obj_rank_result.get("leaguePoints").toString(); // 티어 LP
                    String wins = obj_rank_result.get("wins").toString(); //랭크 전체 승
                    String losses = obj_rank_result.get("losses").toString(); //랭크 전체 패

                    System.out.print(obj_rank_result);
                    System.out.println("솔랭 : " + tier + rank);

                    //view로 값 넘겨주기 (String 값)
                    model.addAttribute("soloTier", tier);
                    model.addAttribute("soloRank", rank);
                    model.addAttribute("soloLeaguePoints", leaguePoints + " LP");
                    model.addAttribute("soloWins", wins + " 승");
                    model.addAttribute("soloLosses", losses + " 패");
                } //자랭일 경우

                if ("RANKED_FLEX_SR".equals(rankType)) {
                    String tier = obj_rank_result.get("tier").toString();
                    String rank = obj_rank_result.get("rank").toString();
                    String leaguePoints = obj_rank_result.get("leaguePoints").toString();
                    String wins = obj_rank_result.get("wins").toString();
                    String losses = obj_rank_result.get("losses").toString();

                    System.out.print(obj_rank_result);
                    System.out.println("자랭 : " + tier + rank);

                    model.addAttribute("flexTier", tier);
                    model.addAttribute("flexRank", rank);
                    model.addAttribute("flexLeaguePoints", leaguePoints + " LP");
                    model.addAttribute("flexWins", wins + " 승");
                    model.addAttribute("flexLosses", losses + " 패");
                }
            }
        }

        // 인게임 정보 current
        //https://kr.api.riotgames.com/lol/spectator/v4/active-games/by-summoner/x2OV0C24um6oOgMaj-jhhpDO1WAlCaH_yqyYLf6SQxIY4g?api_key=RGAPI-2e5cc780-361d-4e27-b253-c0823e44289e
        String curResult = "";
        try {
            String curUrl = "https://kr.api.riotgames.com/lol/spectator/v4/active-games/by-summoner/" + id + "?api_key=" + API_KEY;
            URL url = new URL(curUrl);

            BufferedReader bf;
            HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
            urlconnection.setRequestMethod("GET");
            bf = new BufferedReader(new InputStreamReader(urlconnection.getInputStream(), "UTF-8"));
            System.out.println(bf);

            curResult = bf.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("String 타입 : " + curResult);

        try {

            JSONObject curData = (JSONObject) jsonParser.parse(curResult);

            //curResult를 JSON화 해준 후 인게임 정보 가져오기

            String queueId = curData.get("gameQueueConfigId").toString();

            System.out.println("현재 게임 정보 current" + queueId);

            switch (queueId) {
                case "420":
                    queueId = "솔랭";
                    break;
                case "430":
                    queueId = "일반";
                    break;
                case "440":
                    queueId = "자유랭크";
                    break;
                case "450":
                    queueId = "칼바람";
                    break;
                case "900":
                    queueId = "우르프";
                    break;
            }
            model.addAttribute("current", "yes");
            model.addAttribute("queueId", queueId);

            // participants : xx 부분
            JSONArray partiInGame = (JSONArray) curData.get("participants");

            for (int p = 0; p < partiInGame.size(); p++) {
                System.out.println("플레이어 포문 진입, p = " + p + "/" + partiInGame.size());

                JSONArray inGameArr = (JSONArray) partiInGame.get(p);

                // JSON Array -> JSON Object
                JSONObject inGame = new JSONObject();
                if (inGameArr.size() > 0) {
                    for (int i = 0; i < inGameArr.size(); i++) {
                        inGame = (JSONObject) inGameArr.get(i);
                    }
                }

                //key값 증가. 소환사 이름 sm0.sm1.sm2...값 차례대로 넣어주기
                String sm = "sm" + p;
                //<img SRC="smImgUrl">
                String smImgUrl = "smImgUrl" + p;

                //inGame summoner(p)의 소환사 명
                String inName = (String) inGame.get("summonerName");
                System.out.println(sm + inName);


                try {
                    //inGame summoner(p)의 챔피언 key값
                    String inChampId = inGame.get("championId").toString();
                    String getKey = "K" + inChampId;


                    model.addAttribute(sm, inName);
                    //	model.addAttribute(smImgUrl, ddUrl + ddVer + "/img/champion/"+inChamp+".png");

                } catch (Exception e) {
                    System.out.println("------Dto오류-------------");
                }
            }
            // 게임 중 아님 current 404
        } catch (Exception e) {
            model.addAttribute("current", "404");

        }


        // 검색한 소환사 최근 전적 승률 KDA
        int recentWin = 0;
        int recentLose = 0;
        int recentKill = 0;
        int recentDeath = 0;
        int recentAssist = 0;

        // 매치 리스트 가져오기 matchId
        String matchIdResult = null;
        try {
            String match_urls = "https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/" + puuid + "/ids?start=0&count20&api_key=" + API_KEY;
            System.out.println(match_urls);

            URL url = new URL(match_urls);

            BufferedReader bf;
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            bf = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            System.out.println(bf);

            matchIdResult = bf.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("String 타입 : " + matchIdResult);


        JSONArray json_matchId = (JSONArray) jsonParser.parse(matchIdResult);
        */
/*
         * forEach 반복문 시작구간
         * 설명 : 챔피언, 게입타입, 승패, 게임 시간, KDA, 룬, 스펠, 아이템, 플레이어
         *//*

        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        //매치 당 정보 가져오기 / 20게임 정보의 api 이용 중
        for (int i = 0; i < json_matchId.size() - 15; i++) {
            // list에 넣어줄 HashMap. JSP에서 forEach문으로 넘겨주기 위해 list 사용
            HashMap<String, Object> matchMap = new HashMap<>();

            // forEach로 데이터 받아올 때 필요한 matchId 리스트 //matchId0 ~
            matchMap.put("matchId", json_matchId);

            // 더보기 클릭시 AJAX 서버 전달 할 matchId(i)
            model.addAttribute("matchIdDetail" + i, json_matchId.get(i));
            matchMap.put("matchIdDetail", json_matchId.get(i));

            // i번째 matchId에 대한 정보
            String matchDataUrl = "https://asia.api.riotgames.com/lol/match/v5/matches/" + json_matchId.get(i) + "?api_key=" + API_KEY;
            matchDataUrl = matchDataUrl.replace("\"", "");


            URL url = new URL(matchDataUrl);

            BufferedReader bf;
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            bf = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            System.out.println(bf);

            String matchDataResult = bf.readLine();

            System.out.println(i + " matchId 주소 : " + matchDataUrl);
            //String 자료들을 JsonObject 직렬화..
            JSONObject matchData = (JSONObject) jsonParser.parse(matchDataResult);
            //matchData 중 metaDate : xx 부분
            //JSONObject metaData = matchData.getAsJSONObject("metadata");
            JSONObject metaData = (JSONObject) matchData.get("metadata");
            //metaData 중 participants(플레이어 id) list 가져오기 ['','','']
            //확인		//JsonArray participants = metaData.getAsJsonArray("participants");

            //System.out.println("팀원 puuid (JsonArr) : "+participants);

            //matchData 중 info : xx 부분
            JSONObject info = (JSONObject) matchData.get("info");

            //게임종류(협곡 칼바람 등) //모드 추가 시 추가 필요
            String queueId = info.get("queueId").toString();
            switch (queueId) {
                case "420":
                    queueId = "솔랭";
                    break;
                case "430":
                    queueId = "일반";
                    break;
                case "440":
                    queueId = "자유랭크";
                    break;
                case "450":
                    queueId = "칼바람";
                    break;
                case "900":
                    queueId = "우르프";
                    break;
            } // HashMap<k,V> 로 저장
            matchMap.put("queueId", queueId);


            //게임시간 (길이)(초)
            long gameDuration = Integer.parseInt(info.get("gameDuration").toString());
            int gameMin = (int) gameDuration / 60;
            int gameSec = (int) gameDuration % 60;
            matchMap.put("gameDuration", gameMin + "분" + gameSec + "초");
            System.out.println(gameDuration);
            System.out.println("게임시간" + gameMin + "분" + gameSec + "초");

            //게임 시작시간 (ago) // Date error
*/
/*            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String date = "";
            try {
                date = sdf.format(info.get("gameEndTimestamp").toString());
                System.out.println("date try");
            } catch (Exception e) {
                date = sdf.format(info.get("gameStartTimestamp").toString());
                System.out.println("date catch");
            }
            String now = sdf.format(new Date());
            System.out.println("date : " + date);
            System.out.println("now : " + now);

            Date inDate = sdf.parse(date);
            Date nowDate = sdf.parse(now);
            System.out.println("parse in : "+inDate);
            System.out.println("parse now: "+nowDate);

            //24시간->1일 / 60분->1시간 / 60초->1분 표기
            int days = (int)(nowDate.getTime()-inDate.getTime())/(24*60*60*1000);
            int hours = (int)(nowDate.getTime()-inDate.getTime())/(60*60*1000);
            int min = (int)(nowDate.getTime()-inDate.getTime())/(60*1000);
//	    	int sec = (int)(nowDate.getTime()-inDate.getTime())/(1000%60);


            System.out.println("days : "+days);
            System.out.println("hours : "+hours);
            System.out.println("min : "+min);
            // x달 전 / x일 전 / x시간 전 / x분 전
            if (days!=0) {
                matchMap.put("ago", days+"일 전");
                System.out.println(days+"일 전");
            }else if(hours==0){
                matchMap.put("ago", min+"분 전");
                System.out.println(min+" 분 전");
            }else {
                matchMap.put("ago",hours%24+"시간 전");
                System.out.println(hours%24+"시간 전");
            }
            //확인용
            for(String keys : matchMap.keySet()) {
                System.out.print(keys);
                System.out.print(" : ");
                System.out.print(matchMap.get(keys));
                System.out.print(", ");
            }

 *//*


            */
/*
             * participants 키의 배열['participants':{},] 가져오기(플레이어 당 인게임) // 블루 0~4/ 레드 5~9
             * 플레이어 수 만큼 도는 for문
             *//*


            JSONArray partiInGame = (JSONArray) info.get("participants");
            System.out.println("포문 진입 전 사이즈체크 : "+ partiInGame.size());

            for(int p = 0; p < partiInGame.size(); p++){
                System.out.println("플레이어 포문 진입, p = " + p + "/" + partiInGame.size());
                JSONObject inGame = (JSONObject) partiInGame.get(p);

                //key값 증가. 소환사 이름 sm0.sm1.sm2...값 차례대로 넣어주기
                String sm = "sm"+ p;
                //<img SRC="smImgUrl">
                String smImgUrl = "smImgUrl"+p;
                //inGame summoner(p)의 챔피언
                String inChamp = inGame.get("championName").toString();
                //inGame summoner(p)의 소환사 명
                String inName = inGame.get("summonerName").toString();

                System.out.println(sm + inChamp + inName);

                matchMap.put(sm, inName);
                matchMap.put(smImgUrl, ddUrl + ddVer + "/img/champion/"+inChamp+".png");

 		    	*/
/*
 		    	/ 검색한 소환사(나)의 챔피언 가져오기
 		    	/ (json_name=나)
 		    	*//*

                if(inName.equals(json_name)) { //공백처리 완료된 name
                    matchMap.put("myChamp", inChamp);
                    matchMap.put("myChampUrl", ddUrl + ddVer+"/img/champion/"+inChamp+".png");
                    int myK = Integer.parseInt(inGame.get("kills").toString());
                    int myD = Integer.parseInt(inGame.get("deaths").toString());
                    int myA = Integer.parseInt(inGame.get("assists").toString());
                    matchMap.put("myK", myK);
                    matchMap.put("myD", myD);
                    matchMap.put("myA", myA);

                    if(myD==0) {
                        matchMap.put("myAvg", "Perfect!");
                    }else{
                        double kda = (double)Math.round((myK+myA)/myD*100)/100; //흠,,,ㅜㅜ소수 2째자리,,,안댐
                        matchMap.put("myAvg", "평점 1 : "+kda);
                        System.out.println("kda : "+kda);
                    }

                    // 나의 최근 경기(20) KDA
                    recentKill = recentKill + myK;
                    recentDeath = recentDeath + myD;
                    recentAssist = recentAssist + myA;
                    model.addAttribute("recentKill", recentKill);
                    model.addAttribute("recentDeath", recentDeath);
                    model.addAttribute("recentAssist", recentAssist);
                    if(recentDeath==0) {
                        model.addAttribute("recentKDA","퍼펙트냠냠");
                    }
                    model.addAttribute("recentKDA", (recentKill+recentAssist)/recentDeath);

                    // 단일 경기 승리, 패배
                    Boolean win = Boolean.valueOf(inGame.get("win").toString());
                    if (win) {
                        matchMap.put("win", "WIN");
                    }else {
                        matchMap.put("win", "LOSE");

                    }
                    // 나의 최근 경기(20) 승률
                    if (win) {
                        recentWin = recentWin+1;
                    }else {
                        recentLose = recentLose+1;
                    }
                    model.addAttribute("wins", recentWin);
                    model.addAttribute("lose", recentLose);
                    model.addAttribute("recentTotal", recentWin+recentLose);
                    model.addAttribute("recentRate", recentWin/(recentWin+recentLose));


                    // 나의 inGame 룬
                    JSONObject runes = (JSONObject) inGame.get("perks");
                    System.out.println(runes);
                    JSONArray styles = (JSONArray) runes.get("styles");
                    //	String style = style.get("style").ge
                    System.out.println(styles);

                    //styles 에서 style을 가져와야 함,,,, [{"style":},



                    // 나의 inGame 스펠 [{"summonerId1:""}]

                    for(int s=1;s<3;s++) {
                        String smSpell = "summoner"+s+"Id";
                        smSpell = inGame.get(smSpell).toString();

                        switch (smSpell) {
                            case "1" : smSpell ="SummonerBoost";
                                break;
                            case "3" : smSpell="SummonerExhaust";
                                break;
                            case "4" : smSpell="SummonerFlash";
                                break;
                            case "6" : smSpell="SummonerHaste";
                                break;
                            case "7" : smSpell="SummonerHeal";
                                break;
                            case "11" : smSpell="SummonerSmite";
                                break;
                            case "12" : smSpell="SummonerTeleport";
                                break;
                            case "13" : smSpell="SummonerMana";
                                break;
                            case "14" : smSpell="SummonerDot";
                                break;
                            case "21" : smSpell="SummonerBarrier";
                                break;
                            case "30" : smSpell="SummonerPoroRecall";
                                break;
                            case "31" : smSpell="SummonerPoroThrow";
                                break;
                            case "32" : smSpell="SummonerSnowball";
                                break;
                            case "39" : smSpell="SummonerSnowURFSnowball_Mark";
                                break;
                            case "54" : smSpell="Summoner_UltBook_Placeholder";
                                break;
                        }
                        matchMap.put("smSpell"+s, smSpell);
                        matchMap.put("smSpell"+s+"Url", ddUrl + ddVer+"/img/spell/"+smSpell+".png");
                    }

                    // 나의 inGame item 이미지 [{"item":xx}]
                    for(int t=0; t<7; t++) {
                        String item = "item"+t;
                        String inItem = inGame.get(item).toString(); //itemNum 가져오기 위해 String,불필요 시 int ㄱㄱ

                        // item이 없는 칸 회색템 표시
                        if(inItem.equals("0")) {
                            matchMap.put("myT"+t, "/resources/img/itemNull.png");

                            // inGame 나의 item 설명 (툴팁)
                            // item.json URL 연결
                        }else{
                            System.out.println("아이템 넘버 : "+inItem);
                            matchMap.put("myT"+t, ddUrl+ddVer+"/img/item/"+inItem+".png");

                            // item TOOLTIP 템 정보
                            String itemApi = ddUrl+ddVer+"/data/ko_KR/item.json";
                            URL itemUrl = new URL(itemApi);

                            HttpURLConnection ItemUrlconn = (HttpURLConnection) itemUrl.openConnection();
                            ItemUrlconn.setRequestMethod("GET");
                            BufferedReader itemBf = new BufferedReader(new InputStreamReader(ItemUrlconn.getInputStream(),"UTF-8"));
                            System.out.println("아이템 설명 bf : "+itemBf); //작동 확인용

                            String itemResult = itemBf.readLine();

                            //(item.json) itemResult값 parse해서 JsonObject로 받아오기 K:V
                            JSONObject itemJson = (JSONObject) jsonParser.parse(itemResult);
                            //(item.json) Key값이 data 인 항목 { "data" : xx 부분 }
                            JSONObject itemData = (JSONObject) itemJson.get("data");
                            //(item.json) Key값이 data 안에서 1001인 항목 { "data" : {"1001" : xx 부분 }}
                            JSONObject itemNum = (JSONObject) itemData.get(inItem);
                            //오른 에러!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                            String itemName = itemNum.get("name").toString();
                            String itemDesc = itemNum.get("description").toString();
                            String itemText = itemNum.get("plaintext").toString();

                            matchMap.put("itemTooltip"+t,"<b>"+ itemName+"</b>" +"<br><hr>"+itemDesc+"<br>"+itemText);
                            System.out.println("itemTooltip"+t+" : itemName : "+itemName+" / itemDesc : "+itemDesc+" / itemText : "+itemText);
                        }
                        // ITEM, TOOLTIP 종료
                    }
                    System.out.println("matchMap 종료");
                    list.add(matchMap);

                } // 1 matchId 종료
                model.addAttribute("list",list);
            } // 20 MatchId forEach 반복문 종료


            return "summoner/smnResult";
        }

        // 임시
        return null;
    }
}
*/
