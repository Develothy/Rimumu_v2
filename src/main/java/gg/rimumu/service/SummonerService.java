package gg.rimumu.service;

import gg.rimumu.dto.*;
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

import java.util.ArrayList;
import java.util.List;


@Service
public class SummonerService {

    @Value("${LoL.KEY}")
    private String API_KEY;

    @Value("${DDUrl}")
    private String ddUrl;

    @Value("${DDVer}")
    private String ddVer;

    @Value("${smnUrl}")
    private String smnUrl;

    @Value("${tierUrl}")
    private String tierUrl;

    @Value("${currentUrl}")
    private String currentUrl;

    @Value("${matchesUrl}")
    private String matchesUrl;

    @Value("${matchDtlUrl}")
    private String matchDtlUrl;

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
    public SummonerDto smnSearch(SummonerDto summonerDto, String smn) throws IOException, ParseException {

        String url = smnUrl + smn + "?api_key=" + API_KEY;

        String result = urlConn(url);

        // 존재하는 소환사 // 미존재 시 exception 처리

        //검색 소환사 account 정보 가져오기
        JSONObject accResult = (JSONObject) jsonParser.parse(result);

        smnInfo(summonerDto, accResult);

        return summonerDto;
    }

    //소환사 정보
    public SummonerDto smnInfo(SummonerDto summonerDto, JSONObject accResult) throws IOException, ParseException {

        //result를 Json화 해준 후 이름, 아이콘, 고유id 가져오기
        summonerDto.setName(accResult.get("name").toString());
        summonerDto.setSmLv(Integer.parseInt(accResult.get("summonerLevel").toString()));

        int profileIconId = Integer.parseInt(accResult.get("profileIconId").toString());
        summonerDto.setIconImgUrl(ddUrl + ddVer + "/img/profileicon/" + profileIconId + ".png");

        String id = accResult.get("id").toString();
        summonerDto.setId(id);
        String puuid = accResult.get("puuid").toString();
        summonerDto.setPuuid(puuid);


        // 티어 조회
        getTier(summonerDto, id);

        // 게임중 여부 조회
        //currentGame(summonerDto, id);

        // matchId 최근 20게임
        matchesUrl(summonerDto, puuid);

        // matchDtlList
        matchDtl(summonerDto, id);

        return summonerDto;
    } // smnInfo() 소환사 정보 종료


    // 티어 조회 로직
    public SummonerDto getTier(SummonerDto summonerDto, String id) throws IOException, ParseException {

        String rankUrl = tierUrl + id + "?api_key=" + API_KEY;
        String rankResult = urlConn(rankUrl);

        //언랭일 경우 [] 값 null
        if ("[]".equals(rankResult)) {
            summonerDto.setSoloTier("Unranked");
            summonerDto.setFlexTier("Unranked");
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
                    summonerDto.setSoloTier(obj_rank_result.get("tier").toString()); // 챌, 다이아, 플레 등
                    summonerDto.setSoloRank(obj_rank_result.get("rank").toString()); // 1 ~ 4
                    summonerDto.setSoloLeaguePoints(obj_rank_result.get("leaguePoints").toString()); // 티어 LP
                    summonerDto.setSoloWins(obj_rank_result.get("wins").toString()); //랭크 전체 승
                    summonerDto.setSoloLosses(obj_rank_result.get("losses").toString()); //랭크 전체 패
                }
                //자랭일 경우
                if ("RANKED_FLEX_SR".equals(rankType)) {
                    summonerDto.setFlexTier(obj_rank_result.get("tier").toString());
                    summonerDto.setFlexRank(obj_rank_result.get("rank").toString());
                    summonerDto.setSoloLeaguePoints(obj_rank_result.get("leaguePoints").toString());
                    summonerDto.setFlexWins(obj_rank_result.get("wins").toString());
                    summonerDto.setFlexLosses(obj_rank_result.get("losses").toString());
                }
            } // 솔랭, 자랭 구분 종료
        } // 랭크 정보 등록 종료
        return summonerDto;
    } // smnTier() 티어 죄회 로직 종료

    // GameType 구하기
    public String getGameType(String queueId) {
        switch (queueId) {
            case "420": // 4
                queueId = "솔랭";
                break;
            case "430": // 2
                queueId = "일반";
                break;
            case "440":
                queueId = "자유랭크";
                break;
            case "450": // 60
                queueId = "칼바람";
                break;
            case "70":
                queueId = "단일 챔피언";
                break;
            case "300":
                queueId = "포로왕";
                break;
            case "900":
                queueId = "무작위 우르프";
                break;
            case "1020":
                queueId = "단일챔피언";
                break;
            case "1300":
                queueId = "돌격 넥서스";
                break;
            case "1400":
                queueId = "궁극기 주문서";
                break;
            case "1900":
                queueId = "우르프";
                break;
            case "2000":
                queueId = "튜토리얼 1";
                break;
            case "2010":
                queueId = "튜토리얼 2";
                break;
            case "2020":
                queueId = "튜토리얼 3";
                break;

        }
        return queueId;
    }

    // Spell 구하기
    public String getSpell(String smSpell) {

        switch (smSpell) {
            case "1":
                smSpell = "SummonerBoost";
                break;
            case "3":
                smSpell = "SummonerExhaust";
                break;
            case "4":
                smSpell = "SummonerFlash";
                break;
            case "6":
                smSpell = "SummonerHaste";
                break;
            case "7":
                smSpell = "SummonerHeal";
                break;
            case "11":
                smSpell = "SummonerSmite";
                break;
            case "12":
                smSpell = "SummonerTeleport";
                break;
            case "13":
                smSpell = "SummonerMana";
                break;
            case "14":
                smSpell = "SummonerDot";
                break;
            case "21":
                smSpell = "SummonerBarrier";
                break;
            case "30":
                smSpell = "SummonerPoroRecall";
                break;
            case "31":
                smSpell = "SummonerPoroThrow";
                break;
            case "32":
                smSpell = "SummonerSnowball";
                break;
            case "39":
                smSpell = "SummonerSnowURFSnowball_Mark";
                break;
            case "54":
                smSpell = "Summoner_UltBookPlaceholder";
                break;
            case "55":
                smSpell = "Summoner_UltBookSmitePlaceholder";
                break;
        }
        return smSpell;
    }

    // rune 이미지 주소 변환
    public String getRuneImgUrl(String rune) {

        switch (rune) {
            case "8000":
                rune = "perk-images/Styles/7201_Precision.png";
                break;
            case "8100":
                rune = "perk-images/Styles/7200_Domination.png";
                break;
            case "8200":
                rune = "perk-images/Styles/7202_Sorcery.png";
                break;
            case "8300":
                rune = "perk-images/Styles/7203_Whimsy.png";
                break;
            case "8400":
                rune = "perk-images/Styles/7204_Resolve.png";
                break;
        }
        return rune;
    }


    // current 현재 게임 여부 ---------------
    //ex https://kr.api.riotgames.com/lol/spectator/v4/active-games/by-summoner/x2OV0C24um6oOgMaj-jhhpDO1WAlCaH_yqyYLf6SQxIY4g?api_key=RGAPI-032475c9-844d-4beb-82f9-2a1132ee2666
    public SummonerDto currentGame(SummonerDto summonerDto, String id) throws IOException {

        String curUrl = currentUrl + id + "?api_key=" + API_KEY;
        String curResult = urlConn(curUrl);

        try {
            JSONObject curData = (JSONObject) jsonParser.parse(curResult);

            // exception ------------------------게임중이 아닐 경우 null
            //model.addAttribute("current", "yes"); // yes or no 방식으로 변경 // 기존 404;

            // 현재 게임 중일 경우 실행 //

            // 큐 타입
            String queueId = curData.get("gameQueueConfigId").toString();
            summonerDto.setQueueId(getGameType(queueId));

            // participants : ['x','x'] 부분 arr
            JSONArray partiArr = (JSONArray) curData.get("participants");

            for (int p = 0; p < partiArr.size(); p++) {

                JSONArray partiDtlArr = (JSONArray) partiArr.get(p);

                // JSON Array -> JSON Object
                JSONObject inGame = new JSONObject();
                for (int i = 0; i < partiDtlArr.size(); i++) {

                    // JSON Array -> JSON Object
                    // i번째 participant
                    inGame = (JSONObject) partiDtlArr.get(i);

                    //inGame participant(p)의 id == myId 비교
                    String compareId = (String) inGame.get("summonerId");
                    if (compareId == id) {

                        //inGame participant(p)의 xx값
                        //Long curTime = (Long) inGame.get("gameStartTime"); // 유닉스 타임
                        int inChampId = (int) inGame.get("championId"); //
                        String champImg = (String) ChampionDto.class.getDeclaredField("K" + inChampId).get(inChampId);

                        System.out.println("champDto :" + champImg);

                        //	model.addAttribute(smImgUrl, ddUrl + ddVer + "/img/champion/"+inChamp+".png");

                    }
                }
            }
            // 게임 중 아님 current 404
        } catch (Exception e) {
            //    model.addAttribute("current", "404"); // exception 필요

        }
        return summonerDto;
    }
    // current 현재 게임 여부 종료


    // 매치 리스트 가져오기 matchId
    public SummonerDto matchesUrl(SummonerDto summonerDto, String puuid) throws IOException, ParseException {

        String matUrl = matchesUrl + puuid + "/ids?start=0&count20&api_key=" + API_KEY;
        JSONArray matchesArr = (JSONArray) jsonParser.parse(urlConn(matUrl));
        summonerDto.setMatchIdList(matchesArr);

        System.out.println("matchesUrl() matcheIdList : " + summonerDto.getMatchIdList().toString());
        return summonerDto;
    }

    /*
     * forEach 반복문 시작구간
     * 설명 : 챔피언, 게입타입, 승패, 게임 시간, KDA, 룬, 스펠, 아이템, 플레이어
     */

    public SummonerDto matchDtl(SummonerDto summonerDto, String id) throws ParseException, IOException {

        List<SummonerDto> matchIdList = summonerDto.getMatchIdList();
        List<MatchDto> matchDtoList = new ArrayList<>();

        //매치 당 정보 가져오기 / 20게임 정보의 api 이용 중
        for (int i = 0; i < matchIdList.size() - 15; i++) {

            MatchDto matchDto = new MatchDto();

            String matchId = "";
            matchId = String.valueOf(matchIdList.get(i));
            System.out.println("for문 matchId" + matchId);
            matchDto.setMatchId(matchId);

            // i번째 matchId에 대한 정보
            String matchDataUrl = matchDtlUrl + matchId + "?api_key=" + API_KEY;
            matchDataUrl = matchDataUrl.replace("\"", "");
            System.out.println(i + " matchId 주소 : " + matchDataUrl);

            String matchDataResult = urlConn(matchDataUrl);

            //String 자료들을 JsonObject 직렬화..
            JSONObject matchData = (JSONObject) jsonParser.parse(matchDataResult);

            //matchData 중 metaDate : xx 부분
            JSONObject metaData = (JSONObject) matchData.get("metadata");

            //metaData 중 participants(플레이어 id) list 가져오기 ['','','']
            JSONArray participants = (JSONArray) metaData.get("participants");

            System.out.println("팀원 puuid (JsonArr) : " + participants);

            //matchData 중 info : xx 부분
            JSONObject info = (JSONObject) matchData.get("info");

            //게임종류(협곡 칼바람 등) //모드 추가 시 추가 필요
            String queueId = info.get("queueId").toString();
            matchDto.setQueueId(getGameType(queueId));


            //게임시간 (길이)(초)
            long gameDuration = Integer.parseInt(info.get("gameDuration").toString());
            int gameMin = (int) gameDuration / 60;
            int gameSec = (int) gameDuration % 60;

            matchDto.setGameDuration(gameMin + "분" + gameSec + "초");
            System.out.println(gameDuration);
            System.out.println("게임시간" + gameMin + "분" + gameSec + "초");



            //게임 시작시간 (ago) // Date error

/*            
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
            Date date = new Date();
            Long date1;
            try {
                date1 = Long.valueOf(sdf.format(info.get("gameEndTimestamp")));
                System.out.println("date try");
            } catch (Exception e) {
  //              date = sdf.format(info.get("gameStartTimestamp").toString());
                System.out.println("date catch");
            }
            String now = sdf.format(LocalDateTime.now());
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
*/
            // x달 전 / x일 전 / x시간 전 / x분 전
/*
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
*/


            /*
             * participants 키의 배열['participants':{},] 가져오기(플레이어 당 인게임) // 블루 0~4/ 레드 5~9
             * 플레이어 수 만큼 도는 for문
             */


            JSONArray partiInArr = (JSONArray) info.get("participants");
            System.out.println("포문 진입 전 사이즈체크 : " + partiInArr.size());

            List<ParticipantDto> partiDtoList = new ArrayList<>();

            for (int p = 0; p < partiInArr.size(); p++) {
                System.out.println("참가자 포문 진입, p = " + p + "/" + partiInArr.size());
                JSONObject inGame = (JSONObject) partiInArr.get(p);

                ParticipantDto partiDto = new ParticipantDto();


                //inGame summoner(p)의 소환사 명
                String inName = inGame.get("summonerName").toString();
                partiDto.setInName(inName);
                System.out.println(inName);

                //inGame summoner(p)의 챔피언
                String inChamp = inGame.get("championName").toString();
                partiDto.setInChamp(inChamp);
                partiDto.setChampImgUrl(ddUrl + ddVer + "/img/champion/" + inChamp + ".png");
                System.out.println(inChamp);

                // 해당 parti의 id가 검색된 id인지 비교
                String compareId = inGame.get("summonerId").toString();
                System.out.println(compareId);

                // 검색한 소환사(나)의 챔피언 가져오기
                if (compareId.equals(id)) { // 검색된 id와 비교

                    // 단일 경기 승리, 패배
                    Boolean win = Boolean.valueOf(inGame.get("win").toString());
                    if (win) {
                        matchDto.setWin("WIN");
                        matchDto.setTable("table-primary");
                        summonerDto.setRecentWin(summonerDto.getRecentWin()+1);
                    } else {
                        matchDto.setWin("LOSE");
                        matchDto.setTable("table-danger");
                        summonerDto.setRecentLose(summonerDto.getRecentLose()+1);
                    }

                    MyGameDto myGameDto = new MyGameDto();
                    myGameDto.setMyChamp(inChamp);
                    myGameDto.setMyChampUrl(ddUrl + ddVer + "/img/champion/" + inChamp + ".png");

                    int myK = Integer.parseInt(inGame.get("kills").toString());
                    int myD = Integer.parseInt(inGame.get("deaths").toString());
                    int myA = Integer.parseInt(inGame.get("assists").toString());
                    // 해당 판 KDA
                    myGameDto.setMyK(myK);
                    myGameDto.setMyD(myD);
                    myGameDto.setMyA(myA);
                    // 최근 전적 KDA
                    summonerDto.setRecentKill(summonerDto.getRecentKill()+myK);
                    summonerDto.setRecentDeath(summonerDto.getRecentDeath()+myD);
                    summonerDto.setRecentAssist(summonerDto.getRecentAssist()+myA);
                    summonerDto.setRecentTotal(summonerDto.getRecentTotal()+1);
                    summonerDto.setRecentAvg(summonerDto.getRecentKill()+summonerDto.getRecentAssist()/summonerDto.getRecentDeath());


                    if (myD == 0) {
                        myGameDto.setMyAvg("Perfect!");
                    } else {
                        double kda = (double) Math.round((myK + myA) / myD * 100) / 100; //흠,,,ㅜㅜ소수 2째자리,,,안댐
                        myGameDto.setMyAvg("평점 1 : " + kda);
                        System.out.println("kda : " + kda);
                    }


                    // 나의 inGame 룬
                    JSONObject runes = (JSONObject) inGame.get("perks");
                    JSONArray styles = (JSONArray) runes.get("styles");

                    // 메인 룬
                    JSONObject selec1 = (JSONObject) styles.get(0);
                    myGameDto.setRuneImgUrl1(ddUrl + "img/" + getRuneImgUrl(selec1.get("style").toString()));
                    // 보조 룬
                    JSONObject selec2 = (JSONObject) styles.get(1);
                    myGameDto.setRuneImgUrl2(ddUrl + "img/" + getRuneImgUrl(selec2.get("style").toString()));


                    // 나의 inGame 스펠 [{"summonerId1:""}]
                    for (int s = 1; s < 3; s++) {
                        String smSpell = "";
                        smSpell = inGame.get("summoner"+s+"Id").toString();

                        smSpell = getSpell(smSpell);
                        if (s == 1) {
                            myGameDto.setSpell1(smSpell);
                            myGameDto.setSpImgUrl1(ddUrl + ddVer + "/img/spell/" + smSpell + ".png");
                        } else {
                            myGameDto.setSpell2(smSpell);
                            myGameDto.setSpImgUrl2(ddUrl + ddVer + "/img/spell/" + smSpell + ".png");

                        }
                        System.out.println("spell : " + smSpell);
                    }


                    // 나의 inGame item 이미지 [{"item":xx}]
                    List<ItemDto> itemList = new ArrayList<>();

                    for(int t=0; t<7; t++) {
                        String item = "item" + t;
                        String itemNum = inGame.get(item).toString(); //itemNum 가져오기 위해 String,불필요 시 int ㄱㄱ

                        ItemDto itemDto = new ItemDto();

                        // item이 없는 칸 회색템 표시
                        if (itemNum.equals("0")) {
                            itemDto.setItemNum(itemNum);
                            itemDto.setItemImgUrl("/img/itemNull.png");

                            // inGame 나의 item 설명 (툴팁)
                            // item.json URL 연결
                        } else {
                            System.out.println("아이템 넘버 : " + itemNum);
                            itemDto.setItemNum(itemNum);
                            itemDto.setItemImgUrl(ddUrl + ddVer + "/img/item/" + itemNum + ".png");

                            // item TOOLTIP 템 정보

                            String itemUrl = ddUrl + ddVer + "/data/ko_KR/item.json";
                            String itemResult = urlConn(itemUrl);

                            //(item.json) itemResult값 parse해서 JsonObject로 받아오기 K:V
                            JSONObject itemJson = (JSONObject) jsonParser.parse(itemResult);
                            //(item.json) Key값이 data 인 항목 { "data" : xx 부분 }
                            JSONObject itemData = (JSONObject) itemJson.get("data");
                            //(item.json) Key값이 data 안에서 1001인 항목 { "data" : {"1001" : xx 부분 }}
                            JSONObject itemDtl = (JSONObject) itemData.get(itemNum);
                            //오른 에러!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!8xxx
                            String itemName = itemDtl.get("name").toString();
                            String itemDesc = itemDtl.get("description").toString();
                            String itemText = itemDtl.get("plaintext").toString();

                            itemDto.setItemTooltip("<b>" + itemName + "</b>" + "<br><hr>" + itemDesc + "<br>" + itemText);

                            System.out.println("itemTooltip" + t + " : itemName : " + itemName + " / itemDesc : " + itemDesc + " / itemText : " + itemText);

                        }
                        itemList.add(itemDto);
                        // ITEM, TOOLTIP 종료

                    }
                    myGameDto.setItemDtoList(itemList);
                    matchDto.setMyGameDto(myGameDto);
                }
                System.out.println("partiDto : "+partiDto);
                partiDtoList.add(partiDto);
            } // 1 matchId 종료
            matchDto.setPartiDtoList(partiDtoList);
            System.out.println("partiDtoList : "+partiDtoList.toString());

            //test
            System.out.println("matchDto : "+matchDto);
            matchDtoList.add(matchDto);
            System.out.println("matchDtoList : "+matchDtoList);
            summonerDto.setMatchDtoList(matchDtoList);
        } // 20 MatchId forEach 반복문 종료

        System.out.println("summonerDto : "+summonerDto);
        return summonerDto;
    }


    //System.out.println("matchDtlList 종료 : "+matchDtoList);


}
