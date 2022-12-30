package gg.rimumu.service;

import gg.rimumu.dto.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.*;


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

    final static JSONParser jsonParser = new JSONParser();


    // api 연결
    public String urlConn(String urlConn) throws IOException, ParseException {

        String apiResultString = "";

        URL url = new URL(urlConn);
        BufferedReader bf; //buffer reread 전송 전 임시 보관소(입출력 속도향상)
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        bf = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

        apiResultString = bf.readLine();
        //JSONObject apiResult = (JSONObject) jsonParser.parse(apiResultString);
        return apiResultString;
    }

    // 소환사 검색
    public SummonerDto smnSearch(SummonerDto summonerDto, String smn) throws IOException, ParseException {

        String url = smnUrl + smn + "?api_key=" + API_KEY;

        // 존재하는 소환사 // 미존재 시 exception 처리 필요
        String accResultString= urlConn(url);
        //검색 소환사 account 정보 가져오기
        JSONObject accResult = (JSONObject) jsonParser.parse(accResultString);

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

        // 게임중 여부 조회 (riot developer api 막힘)
        currentGame(summonerDto, id);

        // matchId 최근 20게임
        matchesUrl(summonerDto, puuid);

        // matchDtlList
        matchDtl(summonerDto);

        return summonerDto;
    } // smnInfo() 소환사 정보 종료


    // current 현재 게임 여부 ---------------
    //ex https://kr.api.riotgames.com/lol/spectator/v4/active-games/by-summoner/x2OV0C24um6oOgMaj-jhhpDO1WAlCaH_yqyYLf6SQxIY4g?api_key=RGAPI-032475c9-844d-4beb-82f9-2a1132ee2666
    public SummonerDto currentGame(SummonerDto summonerDto, String id) throws IOException {

        String curUrl = currentUrl + id + "?api_key=" + API_KEY;

        // exception ------------------------게임중이 아닐 경우 null
        // 현재 게임 중일 경우 실행 //
        try {
            String curResultString = urlConn(curUrl);
            JSONObject curResult = (JSONObject) jsonParser.parse(curResultString);
            summonerDto.setCurrent(true);

            // 큐 타입
            String queueId = curResult.get("gameQueueConfigId").toString();
            summonerDto.setQueueId(getGameType(queueId));

            // participants : ['x','x'] 부분 arr
            JSONArray partiArr = (JSONArray) curResult.get("participants");

            for (int p = 0; p < partiArr.size(); p++) {
                //JSONArray partiDtlArr = (JSONArray) partiArr.get(p);

                // i번째 participant
                JSONObject inGame = (JSONObject) partiArr.get(p);
                //inGame participant(p)의 id == myId 비교
                String compareId = (String) inGame.get("summonerId");
                if (compareId.equals(id)) {
                    //Long curTime = (Long) inGame.get("gameStartTime"); // 유닉스 타임
                    String curChamp = ChampionKey.valueOf("K"+inGame.get("championId")).getLabel();
                    String curChampImg = ddUrl + ddVer + "/img/champion/" + curChamp +".png";
                    summonerDto.setCurChamp("현재 " + curChamp + "를 게임중!");
                    summonerDto.setCurChampUrl(curChampImg);
                    return summonerDto;
                }
            }
            // 게임 중 아님 current 404
        } catch (Exception e) {
            System.out.println("not play now");
        }
        return summonerDto;
    }
    // current 현재 게임 여부 종료

    // 티어 조회 로직
    public SummonerDto getTier(SummonerDto summonerDto, String id) throws IOException, ParseException {

        String rankUrl = tierUrl + id + "?api_key=" + API_KEY;
        String rankResultString = urlConn(rankUrl);
        summonerDto.setSoloTier("Unranked");
        summonerDto.setFlexTier("Unranked");

        //언랭아닐 경우 [] 값
        if (ObjectUtils.isEmpty(rankResultString)) {
            JSONArray rankArr = (JSONArray) jsonParser.parse(rankResultString);
            //솔랭, 자랭 구분하기
            for (int i = 0; i < rankArr.size(); i++) {

                JSONObject ranks = (JSONObject) rankArr.get(i);
                String rankType = ranks.get("queueType").toString();

                // 솔랭, 자랭 값이 존재 한다면 해당 tier값으로 덮음
                //솔랭
                if ("RANKED_SOLO_5x5".equals(rankType)) {
                    summonerDto.setSoloTier(ranks.get("tier").toString()); // 챌, 다이아, 플레 등
                    summonerDto.setSoloRank(ranks.get("rank").toString()); // 1 ~ 4
                    summonerDto.setSoloLeaguePoints(ranks.get("leaguePoints").toString()); // 티어 LP
                    summonerDto.setSoloWins(ranks.get("wins").toString()); //랭크 전체 승
                    summonerDto.setSoloLosses(ranks.get("losses").toString()); //랭크 전체 패
                }
                //자랭
                if ("RANKED_FLEX_SR".equals(rankType)) {
                    summonerDto.setFlexTier(ranks.get("tier").toString());
                    summonerDto.setFlexRank(ranks.get("rank").toString());
                    summonerDto.setFlexLeaguePoints(ranks.get("leaguePoints").toString());
                    summonerDto.setFlexWins(ranks.get("wins").toString());
                    summonerDto.setFlexLosses(ranks.get("losses").toString());
                }
            } // 솔랭, 자랭 구분 종료
        } // 랭크 정보 등록 종료
        return summonerDto;
    } // smnTier() 티어 죄회 로직 종료

    // GameType 구하기
    public String getGameType(String queueId) {

        return GameTypeKey.valueOf("T"+queueId).label();
    }

    // 매치 리스트 가져오기 matchId
    public SummonerDto matchesUrl(SummonerDto summonerDto, String puuid) throws IOException, ParseException {

        String matUrl = matchesUrl + puuid + "/ids?start=0&count20&api_key=" + API_KEY;
        String matchesString = urlConn(matUrl);
        JSONArray matchesArr = (JSONArray) jsonParser.parse(matchesString);
        summonerDto.setMatchIdList(matchesArr);

        System.out.println("matchesUrl() matcheIdList : " + summonerDto.getMatchIdList().toString());
        return summonerDto;
    }

    // match 당 정보 //  { info : {xx} } 부분
    public JSONObject matchIdInfo(String matchId) throws IOException, ParseException {

        String matchDataUrl = matchDtlUrl + matchId + "?api_key=" + API_KEY;
        matchDataUrl = matchDataUrl.replace("\"", "");

        String matchResultString = urlConn(matchDataUrl);
        JSONObject matchResult = (JSONObject) jsonParser.parse(matchResultString);
        //matchResult 중 info : xx 부분
        JSONObject info = (JSONObject) matchResult.get("info");

        return info;
    }

    // Spell 구하기
    public List<String> getSpell(JSONObject inGame) {

        List spImgList = new ArrayList<>();
        for (int s = 1; s < 3; s++){
            String smSpell = inGame.get("summoner" + s + "Id").toString();
            smSpell = SpellKey.valueOf("SP" + smSpell).label();
            spImgList.add(smSpell);
        }
        return spImgList;
    }

    // rune 구하기
    public List<String> getRune(JSONObject inGame){

        List<String> runeList = new ArrayList<>();
        // 나의 inGame 룬
        JSONObject runes = (JSONObject) inGame.get("perks");
        JSONArray styles = (JSONArray) runes.get("styles");

        // 메인 룬
        JSONObject selec1 = (JSONObject) styles.get(0);
        String runeImgUrl1 = ddUrl + "img/" + getRuneImgUrl(selec1.get("style").toString());
        // 보조 룬
        JSONObject selec2 = (JSONObject) styles.get(1);
        String runeImgUrl2 = ddUrl + "img/" + getRuneImgUrl(selec2.get("style").toString());
        runeList.add(runeImgUrl1);
        runeList.add(runeImgUrl2);

        return runeList;
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

    // item 구하기
    public ItemDto getItem(String itemNum) throws IOException, ParseException {

        ItemDto itemDto = new ItemDto();

        // item이 없는 칸 회색템 표시
        if (itemNum.equals("0")) {
            itemDto.setItemNum(itemNum);
            itemDto.setItemImgUrl("/img/itemNull.png");
            itemDto.setItemTooltip("보이지 않는 검이 가장 무서운 법.....");
            return itemDto;
        }
        // inGame 나의 item 설명 (툴팁)
        // item.json URL 연결

        itemDto.setItemNum(itemNum);
        itemDto.setItemImgUrl(ddUrl + ddVer + "/img/item/" + itemNum + ".png");

        // item TOOLTIP 템 정보
        String itemUrl = ddUrl + ddVer + "/data/ko_KR/item.json";

        //(item.json) itemResult값 parse해서 JsonObject로 받아오기 K:V
        String itemResultString = urlConn(itemUrl);
        JSONObject itemResult = (JSONObject) jsonParser.parse(itemResultString);
        //(item.json) Key값이 data 인 항목 { "data" : xx 부분 }
        JSONObject itemData = (JSONObject) itemResult.get("data");
        //(item.json) Key값이 data 안에서 1001인 항목 { "data" : {"1001" : xx 부분 }}
        JSONObject itemDtl = (JSONObject) itemData.get(itemNum);

        String itemName = itemDtl.get("name").toString();
        String itemDesc = itemDtl.get("description").toString();
        String itemText = itemDtl.get("plaintext").toString();

        itemDto.setItemTooltip("<b>" + itemName + "</b>" + "/n <hr>" + itemDesc + "<br>" + itemText);

        return itemDto;
    }

    // match 소환사들 detail_ 소환사명, 챔피언 정보
    public ParticipantDto getPartiNameAndChamp(JSONObject inGame){

        ParticipantDto partiDto = new ParticipantDto();

        //inGame summoner(p)의 소환사 명
        String inName = inGame.get("summonerName").toString();
        partiDto.setInName(inName);

        //inGame summoner(p)의 챔피언
        String inChamp = inGame.get("championName").toString();
        partiDto.setInChamp(inChamp);
        partiDto.setChampImgUrl(ddUrl + ddVer + "/img/champion/" + inChamp + ".png");

        return partiDto;
    }

    public String getKdaAvg (double k, double d, double a) {
        String avg="";
        if (d == 0) {
            avg = "Perfect!";
        } else {
            avg = String.format("%.2f", (k + a) / d);
        }
        return avg;
    }

    public String getAgoTime(Long gameTime) {

        String agoTime = "";

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
        return agoTime;
    }


    /*
     * forEach 반복문 시작구간
     * 설명 : 챔피언, 게입타입, 승패, 게임 시간, KDA, 룬, 스펠, 아이템, 플레이어
     */
    public SummonerDto matchDtl(SummonerDto summonerDto) throws ParseException, IOException {

        List<SummonerDto> matchIdList = summonerDto.getMatchIdList();
        List<MatchDto> matchDtoList = new ArrayList<>();

        //매치 당 정보 가져오기 / 20게임 정보의 api 이용 중
        for (int i = 0; i < matchIdList.size() - 15; i++) {

            String matchId = "";
            matchId = String.valueOf(matchIdList.get(i));
            System.out.println("for문 matchId" + matchId);

            MatchDto matchDto = new MatchDto();
            matchDto.setMatchId(matchId);

            // i번째 matchId에 대한 정보

            //matchData 중 info : xx 부분
            JSONObject info = matchIdInfo(matchId);

            //게임종류(협곡 칼바람 등) //모드 추가 시 추가 필요
            matchDto.setQueueId(getGameType(info.get("queueId").toString()));

            //게임시간 (길이)(초)
            long gameDuration = Integer.parseInt(info.get("gameDuration").toString());
            int gameMin = (int) gameDuration / 60;
            int gameSec = (int) gameDuration % 60;

            matchDto.setGameDuration(gameMin + "분" + gameSec + "초");
            System.out.println(gameDuration);
            System.out.println("게임시간" + gameMin + "분" + gameSec + "초");

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
                ParticipantDto partiDto = getPartiNameAndChamp(inGame);

                // 해당 parti의 id가 검색된 id인지 비교
                // 검색한 소환사(나)의 챔피언 가져오기
                String compareId = partiDto.getInName();
                String name = summonerDto.getName();
                if (name.equals(compareId)) { // 검색된 id와 비교

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
                    String inChamp = partiDto.getInChamp();
                    myGameDto.setMyChamp(inChamp);
                    myGameDto.setMyChampUrl(ddUrl + ddVer + "/img/champion/" + inChamp + ".png");

                    // KDA
                    int myK = Integer.parseInt(inGame.get("kills").toString());
                    int myD = Integer.parseInt(inGame.get("deaths").toString());
                    int myA = Integer.parseInt(inGame.get("assists").toString());
                    // 해당 판 KDA
                    myGameDto.setMyK(myK);
                    myGameDto.setMyD(myD);
                    myGameDto.setMyA(myA);
                    myGameDto.setMyAvg(getKdaAvg(myK, myD, myA));
                    System.out.println("myAvg : " + myGameDto.getMyAvg());
                    // 최근 전적 KDA
                    summonerDto.setRecentKill(summonerDto.getRecentKill()+myK);
                    summonerDto.setRecentDeath(summonerDto.getRecentDeath()+myD);
                    summonerDto.setRecentAssist(summonerDto.getRecentAssist()+myA);
                    summonerDto.setRecentTotal(summonerDto.getRecentTotal()+1);
                    summonerDto.setRecentAvg(getKdaAvg(summonerDto.getRecentKill(), summonerDto.getRecentAssist(), summonerDto.getRecentDeath()));

                    // 나의 inGame 룬
                    myGameDto.setRuneImgUrl1(getRune(inGame).get(0));
                    myGameDto.setRuneImgUrl2(getRune(inGame).get(1));

                    // 나의 inGame 스펠 [{"summonerId1:""}]
                    myGameDto.setSpImgUrl1(ddUrl + ddVer + "/img/spell/" + getSpell(inGame).get(0) + ".png");
                    myGameDto.setSpImgUrl2(ddUrl + ddVer + "/img/spell/" + getSpell(inGame).get(1) + ".png");

                    // 나의 inGame item 이미지 [{"item":xx}]
                    List<ItemDto> itemList = new ArrayList<>();

                    for(int t=0; t<7; t++) {
                        String item = "item" + t;
                        String itemNum = inGame.get(item).toString(); //itemNum 가져오기 위해 String,불필요 시 int ㄱㄱ

                        ItemDto itemDto = getItem(itemNum);

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
