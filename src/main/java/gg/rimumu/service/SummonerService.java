package gg.rimumu.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gg.rimumu.common.ChampionKey;
import gg.rimumu.common.GameTypeKey;
import gg.rimumu.common.RimumuKey;
import gg.rimumu.common.SpellKey;
import gg.rimumu.dto.*;
import gg.rimumu.exception.RimumuException;
import gg.rimumu.util.DateTimeUtil;
import gg.rimumu.util.HttpConnUtil;
import gg.rimumu.util.VersionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Stream;


@Service
public class SummonerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SummonerService.class);

    static final Gson gson = new Gson();

    // 소환사 검색
    public Summoner smnSearch(String smn, int offset) throws RimumuException{
        String url = RimumuKey.SUMMONER_INFO_URL + smn;

        HttpResponse<String> smnSearchResponse = HttpConnUtil.sendHttpGetRequest(url);
        String accResultStr;

        switch (smnSearchResponse.statusCode()) {
            case 200 -> accResultStr = smnSearchResponse.body();
            case 404 -> throw new RimumuException.SummonerNotFoundException(smn);
            default -> throw new RimumuException.ServerException();
        }

        // 검색 소환사 account 정보 가져오기
        Summoner summoner = gson.fromJson(accResultStr, Summoner.class);
        smnInfo(summoner, offset);

        return summoner;
    }

    //소환사 정보
    public Summoner smnInfo(Summoner summoner, int offset) throws RimumuException.MatchNotFoundException {

        // 아이콘 이미지 주소
        summoner.setIconImgUrl(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/profileicon/" + summoner.getProfileIconId() + ".png");

        // 티어 조회
        getTier(summoner);

        // 게임중 여부 조회 (riot developer api 막힘)
        currentGame(summoner);

        // matchId 최근 20게임
        matchesUrl(summoner, offset);


        // matchDtlList
        matchDtls(summoner);

        return summoner;
    } // smnInfo() 소환사 정보 종료


    // current 현재 게임 여부 ---------------
    public Summoner currentGame(Summoner summoner) {

        String curUrl = RimumuKey.SUMMONER_CURRENT_URL + summoner.getId();

        // 현재 게임 중일 경우 실행 //
        try {
            String curResultStr;

            HttpResponse<String> smnSearchResponse = HttpConnUtil.sendHttpGetRequest(curUrl);

            switch (smnSearchResponse.statusCode()) {
                case 200 -> curResultStr = smnSearchResponse.body();
                default -> {
                    LOGGER.info("Not playing now");
                    return summoner;
                }
            }

            JsonObject curResult = gson.fromJson(curResultStr, JsonObject.class);
            summoner.setCurrent(true);

            // 큐 타입
            String queueId = curResult.get("gameQueueConfigId").getAsString();
            summoner.setQueueId(getGameType(queueId));

            // participants : ['x','x'] 부분 arr
            JsonArray partiArr = curResult.getAsJsonArray("participants");

            for (JsonElement parti : partiArr) {

                // i번째 participant
                JsonObject inGame = parti.getAsJsonObject();
                //inGame participant(p)의 id == myId 비교
                String compareId = inGame.get("summonerId").getAsString();
                if (compareId.equals(summoner.getId())) {
                    String curChamp = ChampionKey.valueOf("K"+inGame.get("championId")).getLabel();
                    String curChampImg = RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/champion/" + curChamp +".png";
                    summoner.setCurChamp("현재 " + curChamp + " 게임중!");
                    summoner.setCurChampUrl(curChampImg);
                    return summoner;
                }
            }
        } catch (Exception e) {
            LOGGER.error("인게임 체크 중 오류 발생");
        }
        return summoner;
    }
    // current 현재 게임 여부 종료

    // 티어 조회 로직
    public Summoner getTier(Summoner summoner) {

        String rankUrl = RimumuKey.SUMMONER_TIER_URL + summoner.getId();
        String rankResultStr = (String) HttpConnUtil.sendHttpGetRequest(rankUrl).body();

        //언랭아닐 경우 [] 값
        if (!ObjectUtils.isEmpty(rankResultStr)) {
            JsonArray rankArr = gson.fromJson(rankResultStr, JsonArray.class);
            //솔랭, 자랭 구분하기
            for (int i = 0; i < rankArr.size(); i++) {

                JsonObject ranks = rankArr.get(i).getAsJsonObject();
                String rankType = ranks.get("queueType").getAsString();

                // 솔랭, 자랭 값이 존재 한다면 해당 tier값으로 덮음
                //솔랭
                if ("RANKED_SOLO_5x5".equals(rankType)) {
                    summoner.setSoloTier(ranks.get("tier").getAsString()); // 챌, 다이아, 플레 등
                    summoner.setSoloRank(ranks.get("rank").getAsString()); // 1 ~ 4
                    summoner.setSoloLeaguePoints(ranks.get("leaguePoints").getAsString()); // 티어 LP
                    summoner.setSoloWins(ranks.get("wins").getAsString()); //랭크 전체 승
                    summoner.setSoloLosses(ranks.get("losses").getAsString()); //랭크 전체 패
                }
                //자랭
                else if ("RANKED_FLEX_SR".equals(rankType)) {
                    summoner.setFlexTier(ranks.get("tier").getAsString());
                    summoner.setFlexRank(ranks.get("rank").getAsString());
                    summoner.setFlexLeaguePoints(ranks.get("leaguePoints").getAsString());
                    summoner.setFlexWins(ranks.get("wins").getAsString());
                    summoner.setFlexLosses(ranks.get("losses").getAsString());
                }
            } // 솔랭, 자랭 구분 종료
        } // 랭크 정보 등록 종료
        return summoner;
    } // smnTier() 티어 죄회 로직 종료

    // GameType 구하기
    public String getGameType(String queueId) {

        return GameTypeKey.valueOf("T"+queueId).label();
    }

    // 매치 리스트 가져오기 matchId
    public Summoner matchesUrl(Summoner summoner, int offset) throws RimumuException.MatchNotFoundException {

        String matUrl = RimumuKey.SUMMONER_MATCHES_URL + summoner.getPuuid() + "/ids?start=" + offset + "&count20";
        HttpResponse<String> smnMatchResponse = HttpConnUtil.sendHttpGetRequest(matUrl);

        String matchesStr;

        switch (smnMatchResponse.statusCode()) {
            case 200 -> matchesStr = smnMatchResponse.body();
            default -> {
                LOGGER.error("Match List 정보를 찾을 수 없습니다.");
                throw new RimumuException.MatchNotFoundException(summoner.getName());
            }
        }
        List<Summoner> matchesArr = gson.fromJson(matchesStr, List.class);
        summoner.setMatchIdList(matchesArr);

        LOGGER.info("matchesUrl() matcheIdList : " + summoner.getMatchIdList().toString());
        return summoner;
    }

    // match 당 정보 //  { info : {xx} } 부분
    public JsonObject getMatchIdInfo(String matchId) throws RimumuException.MatchNotFoundException {

        String matchDataUrl = RimumuKey.SUMMONER_MATCHDTL_URL + matchId.replace("\"", "");

        HttpResponse<String> MatchInfoResponse = HttpConnUtil.sendHttpGetRequest(matchDataUrl);

        String matchResultStr;

        switch (MatchInfoResponse.statusCode()) {
            case 200 -> matchResultStr = MatchInfoResponse.body();
            default -> {
                LOGGER.error("Match 정보를 찾을 수 없습니다.");
                throw new RimumuException.MatchNotFoundException(matchId);
            }
        }

        JsonObject matchResult = gson.fromJson(matchResultStr, JsonObject.class);
        //matchResult 중 info : xx 부분
        JsonObject info = matchResult.getAsJsonObject("info");

        return info;
    }

    // Spell 구하기
    public List<String> getSpell(JsonObject inGame) {

        List spImgList = new ArrayList<>();
        for (int s = 1; s < 3; s++){
            String smSpell = inGame.get("summoner" + s + "Id").getAsString();
            smSpell = SpellKey.valueOf("SP" + smSpell).label();
            spImgList.add(smSpell);
        }
        return spImgList;
    }

    // rune 구하기
    public List<String> getRune(JsonObject inGame){

        List runeList = new ArrayList<>();
        // 나의 inGame 룬
        JsonObject runes = inGame.getAsJsonObject("perks");
        JsonArray styles = runes.getAsJsonArray("styles");

        // 메인 룬
        JsonObject selec1 = (JsonObject) styles.get(0);
        String runeImgUrl1 = RimumuKey.DD_URL + "img/" + getRuneImgUrl(selec1.get("style").getAsString());
        // 보조 룬
        JsonObject selec2 = (JsonObject) styles.get(1);
        String runeImgUrl2 = RimumuKey.DD_URL + "img/" + getRuneImgUrl(selec2.get("style").getAsString());
        runeList.add(runeImgUrl1);
        runeList.add(runeImgUrl2);

        return runeList;
    }

    // rune 이미지 주소 변환
    public String getRuneImgUrl(String rune) {

        switch (rune) {
            case "8000" -> rune = "perk-images/Styles/7201_Precision.png";
            case "8100" -> rune = "perk-images/Styles/7200_Domination.png";
            case "8200" -> rune = "perk-images/Styles/7202_Sorcery.png";
            case "8300" -> rune = "perk-images/Styles/7203_Whimsy.png";
            case "8400" -> rune = "perk-images/Styles/7204_Resolve.png";
        }
        return rune;
    }

    // item 구하기
    public Item getItem(int itemNum) {

        Item item = new Item();

        // item이 없는 칸 회색템 표시
        if (itemNum == 0) {
            item.setItemNum(itemNum);
            item.setItemImgUrl("/img/itemNull.png");
            item.setItemTooltip("보이지 않는 검이 가장 무서운 법.....");
            return item;
        }
        // inGame 나의 item 설명 (툴팁)
        // item.json URL 연결

        item.setItemNum(itemNum);
        item.setItemImgUrl(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/item/" + itemNum + ".png");

        // item TOOLTIP 템 정보
        String itemUrl = RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/data/ko_KR/item.json";

        //(item.json) itemResult값 parse해서 JsonObject로 받아오기 K:V
        String itemResultStr = (String) HttpConnUtil.sendHttpGetRequest(itemUrl).body();
        JsonObject itemResult = gson.fromJson(itemResultStr, JsonObject.class);
        //(item.json) Key값이 data 인 항목 { "data" : xx 부분 }
        JsonObject itemData = itemResult.getAsJsonObject("data");
        //(item.json) Key값이 data 안에서 1001인 항목 { "data" : {"1001" : xx 부분 }}
        JsonObject itemDtl = itemData.getAsJsonObject(String.valueOf(itemNum));

        String itemName = itemDtl.get("name").getAsString();
        String itemDesc = itemDtl.get("description").getAsString();
        String itemText = itemDtl.get("plaintext").getAsString();

        item.setItemTooltip("<b>" + itemName + "</b>" + "/n <hr>" + itemDesc + "<br>" + itemText);

        return item;
    }

    // match 소환사들 detail_ 소환사명, 챔피언 정보
    public List getPartiNameAndChamp(JsonObject inGame){

        List<String> nameAndChamp = new ArrayList<>();
        //inGame summoner(p)의 소환사 명, 챔피언
        nameAndChamp.add(inGame.get("summonerName").getAsString());
        nameAndChamp.add(inGame.get("championId").getAsString());

        return nameAndChamp;
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


    /*
     * forEach 반복문 시작구간
     * 설명 : 챔피언, 게입타입, 승패, 게임 시간, KDA, 룬, 스펠, 아이템, 플레이어
     */
    public Summoner matchDtls(Summoner summoner) throws RimumuException.MatchNotFoundException {

        List<Summoner> matchIdList = summoner.getMatchIdList();
        List<Match> matchList = new ArrayList<>();

        //매치 당 정보 가져오기 / 20게임 정보의 api 이용 중
        for (int i = 0; i < matchIdList.size() - 15; i++) {

            String matchId = "";
            matchId = String.valueOf(matchIdList.get(i));
            LOGGER.info("{}번 Match : {}", i, matchId);

            Match match = new Match();
            match.setMatchId(matchId);

            // i번째 matchId에 대한 정보

            //matchData 중 info : xx 부분
            JsonObject info = getMatchIdInfo(matchId);

            //게임종류(협곡 칼바람 등) //모드 추가 시 추가 필요
            match.setQueueId(getGameType(info.get("queueId").getAsString()));

            //게임시간
            long gameDuration = info.get("gameDuration").getAsLong();
            match.setGameDuration(DateTimeUtil.toDuration(gameDuration));
            long gameStarted = info.get("gameStartTimestamp").getAsLong() / 1000;
            match.setGamePlayedAt(DateTimeUtil.fromBetweenNow(gameStarted) + " 전");

            LOGGER.info("== 게임시간 : {}", match.getGameDuration());
            LOGGER.info("== {}", match.getGamePlayedAt());

            /*
             * participants 키의 배열['participants':{},] 가져오기(플레이어 당 인게임) // 블루 0~4/ 레드 5~9
             * 플레이어 수 만큼 도는 for문
             */
            JsonArray partiInArr = info.getAsJsonArray("participants");
            LOGGER.info("== Participants  사이즈 체크 : {}", partiInArr.size());

            List<Participant> Participants = new ArrayList<>();

            for (JsonElement parti : partiInArr) {

                JsonObject inGame = parti.getAsJsonObject();
                Participant participant = new Participant();

                List<String> nameAndChamp = getPartiNameAndChamp(inGame);
                participant.setInName(nameAndChamp.get(0));
                // 챔프네임의 대소문자가 match Json과 img API가 동일하지 않은 이유로 에러발생. 때문에 emun에서 가져옴
                String champ = ChampionKey.valueOf("K" + nameAndChamp.get(1)).label();
                participant.setInChamp(champ);
                participant.setChampImgUrl(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/champion/" + champ + ".png");

                // 해당 parti의 id가 검색된 id인지 비교
                if (summoner.getName().equals(participant.getInName())) {

                    // participant가 나일 경우 추가 정보 세팅
                    setMyGame(summoner, match, inGame, champ);
                }

                Participants.add(participant);

            } // 1 matchId 종료
            match.setParticipants(Participants);
            matchList.add(match);
            summoner.setMatchList(matchList);
        }
        LOGGER.info("{} 조회 종료", summoner.getName());
        return summoner;
    }

    private void setMyGame(Summoner summoner, Match match, JsonObject inGame, String inChamp) {

        // 단일 경기 승리, 패배
        Boolean win = inGame.get("win").getAsBoolean();
        if (win) {
            match.setWin("WIN");
            match.setTable("table-primary");
            summoner.setRecentWin(summoner.getRecentWin()+1);
        } else {
            match.setWin("LOSE");
            match.setTable("table-danger");
            summoner.setRecentLose(summoner.getRecentLose()+1);
        }

        MyGame myGame = new MyGame();
        myGame.setInChamp(inChamp);
        myGame.setChampImgUrl(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/champion/" + inChamp + ".png");

        // KDA
        int myK = inGame.get("kills").getAsInt();
        int myD = inGame.get("deaths").getAsInt();
        int myA = inGame.get("assists").getAsInt();

        // 해당 판 KDA
        myGame.setKill(myK);
        myGame.setDeath(myD);
        myGame.setAssist(myA);
        myGame.setAvg(getKdaAvg(myK, myD, myA));
        LOGGER.info("== myAvg : {}", myGame.getAvg());
        // 최근 전적 KDA
        summoner.setRecentKill(summoner.getRecentKill()+myK);
        summoner.setRecentDeath(summoner.getRecentDeath()+myD);
        summoner.setRecentAssist(summoner.getRecentAssist()+myA);
        summoner.setRecentTotal(summoner.getRecentTotal()+1);
        summoner.setRecentAvg(getKdaAvg(summoner.getRecentKill(), summoner.getRecentAssist(), summoner.getRecentDeath()));

        // 나의 inGame 룬
        List<String> runes = getRune(inGame);
        myGame.setRuneImgUrl1(runes.get(0));
        myGame.setRuneImgUrl2(runes.get(1));

        // 나의 inGame 스펠 [{"summonerId1:""}]
        List<String> spells = getSpell(inGame);
        myGame.setSpImgUrl1(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/spell/" + spells.get(0) + ".png");
        myGame.setSpImgUrl2(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/spell/" + spells.get(1) + ".png");

        // 나의 inGame item 이미지 [{"item":xx}]
        List<Item> itemList = Stream.iterate(0, t -> t < 7, t -> t + 1)
                .map(t -> "item" + t)
                .map(inGame::get)
                .map(JsonElement::getAsInt)
                .map(this::getItem)
                .toList();

        myGame.setItemList(itemList);
        match.setMyGame(myGame);
    }
}
