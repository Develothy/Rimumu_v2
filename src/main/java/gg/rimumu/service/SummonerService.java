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
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;


@Service
public class SummonerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SummonerService.class);
    private static final Gson gson = new Gson();


    // 소환사 검색
    public Summoner smnSearch(String smn) throws RimumuException {

        Summoner summoner = getSummoner(smn);
        setSmnInfo(summoner);

        return summoner;
    }

    public Summoner getSummoner(String smn) throws RimumuException {
        String url = RimumuKey.SUMMONER_INFO_URL + smn;
        Summoner summoner = new Summoner();

        try {
            HttpResponse<String> smnSearchResponse = HttpConnUtil.sendHttpGetRequest(url);

            // 검색 소환사 account 정보 가져오기
            summoner = gson.fromJson(smnSearchResponse.body(), Summoner.class);
        } catch (RimumuException e) {
            LOGGER.error("!! getSummoner smnSearch error: {}", e.getMessage());
            throw new RimumuException.SummonerNotFoundException(smn);
        }
        return summoner;
    }

    //소환사 정보
    public void setSmnInfo(Summoner summoner){

        // 아이콘 이미지 주소
        summoner.setIconImgUrl(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/profileicon/" + summoner.getProfileIconId() + ".png");

        // 티어 조회
        getTier(summoner);

        // 게임중 여부 조회 (riot developer api 막힘)
        checkCurrentGame(summoner);
    }

    public String getSmnPuuid(String smn) throws RimumuException {
        Summoner summoner = getSummoner(smn);
        return summoner.getPuuid();
    }


    public List<String> getMatches(Summoner summoner, int offset ) throws RimumuException, ExecutionException, InterruptedException {

        return getMatchesUrl(summoner, offset);
    }


    // current 현재 게임 여부 ---------------
    public Summoner checkCurrentGame(Summoner summoner) {

        String curUrl = RimumuKey.SUMMONER_CURRENT_URL + summoner.getId();

        JsonObject curResult;
        try {
            HttpResponse<String> smnSearchResponse = HttpConnUtil.sendHttpGetRequest(curUrl);
            if (200 != smnSearchResponse.statusCode()) {
                LOGGER.info("Not playing now");
                return summoner;
            }

            curResult = gson.fromJson(smnSearchResponse.body(), JsonObject.class);
            summoner.setCurrent(true);

        } catch (RimumuException e) {
            return summoner;
        }

        // 현재 게임 중일 경우 실행 //

        // 큐 타입
        String queueId = curResult.get("gameQueueConfigId").getAsString();
        summoner.setQueueId(getGameType(queueId));

        // participants : ['x','x'] 부분 arr
        JsonArray partiArr = curResult.getAsJsonArray("participants");

        for (JsonElement parti : partiArr) {
            // i번째 participant
            JsonObject inGame = parti.getAsJsonObject();
            //inGame participant(p)의 id == myId 비교
            if (summoner.getId().equals(inGame.get("summonerId").getAsString())) {
                String curChamp = ChampionKey.valueOf("K" + inGame.get("championId")).getLabel();
                String curChampImg = RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/champion/" + curChamp +".png";
                summoner.setCurChamp(curChamp);
                summoner.setCurChampUrl(curChampImg);
                return summoner;
            }
        }
        return summoner;
    }
    // current 현재 게임 여부 종료

    // 티어 조회 로직
    public Summoner getTier(Summoner summoner) {

        String rankUrl = RimumuKey.SUMMONER_TIER_URL + summoner.getId();
        String rankResultStr = null;

        try {
            HttpResponse<String> rankResultResponse = HttpConnUtil.sendHttpGetRequest(rankUrl);
            rankResultStr = rankResultResponse.body();
        } catch (RimumuException e) {
            LOGGER.error("!! getTier rankResultResponse error", e.getMessage());
        }

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
    public List<String> getMatchesUrl(Summoner summoner, int offset) throws RimumuException {

        String userPuuid = summoner.getPuuid();
        String matUrl = RimumuKey.SUMMONER_MATCHES_URL + userPuuid + "/ids?start=" + offset + "&count=10";
        try {
            HttpResponse<String> smnMatchResponse = HttpConnUtil.sendHttpGetRequest(matUrl);
            List<String> matcheIds = gson.fromJson(smnMatchResponse.body(), List.class);

            if (ObjectUtils.isEmpty(matcheIds)) {
                throw new RimumuException();
            }
            return matcheIds;

        } catch (RimumuException e) {
            throw new RimumuException.MatchNotFoundException(userPuuid);
        }
    }

    // match 당 정보 //  { info : {xx} } 부분
    public JsonObject getMatchIdInfo(String matchId) throws RimumuException {

        String matchDataUrl = RimumuKey.SUMMONER_MATCHDTL_URL + matchId.replace("\"", "");

        try {
            HttpResponse<String> matchInfoResponse = HttpConnUtil.sendHttpGetRequest(matchDataUrl);
            JsonObject matchResult = gson.fromJson(matchInfoResponse.body(), JsonObject.class);
            //matchResult 중 info : xx 부분
            JsonObject info = matchResult.getAsJsonObject("info");
            return info;

        } catch (RimumuException e) {
            throw new RimumuException.MatchNotFoundException(matchId);
        }
    }

    // Spell 구하기
    public Map<String, String> getSpell(JsonObject inGame) {

        Map<String, String> spell = new HashMap<>();
        for (int s = 1; s < 3; s++){
            String smSpell = inGame.get("summoner" + s + "Id").getAsString();
            smSpell = SpellKey.valueOf("SP" + smSpell).label();
            spell.put("spell" + s, smSpell);
        }
        return spell;
    }

    // rune 구하기
    public Map<String, String> getRune(JsonObject inGame){

        Map<String, String> rune = new HashMap<>();
        // 나의 inGame 룬
        JsonObject runes = inGame.getAsJsonObject("perks");
        JsonArray styles = runes.getAsJsonArray("styles");

        String rune1 = ((JsonObject) styles.get(0)).get("style").getAsString();;
        String rune2;
        String runeImgUrl1;
        String runeImgUrl2;
        // 메인 룬
        if (!"0".equals(rune1)) {
            runeImgUrl1 = RimumuKey.DD_URL + "img/" + getRuneImgUrl(rune1);
            // 보조 룬
            rune2 = ((JsonObject) styles.get(1)).get("style").getAsString();
            runeImgUrl2 = RimumuKey.DD_URL + "img/" + getRuneImgUrl(rune2);

        } else {
            rune1 = null;
            rune2 = null;
            runeImgUrl1 = null;
            runeImgUrl2 = null;
            /*            rune1 = inGame.get("playerAugment1").getAsString();
            rune2 = inGame.get("playerAugment2").getAsString();
            runeImgUrl1 = RimumuKey.DD_URL + "img/" + getRuneImgUrl(rune1);
            runeImgUrl2 = RimumuKey.DD_URL + "img/" + getRuneImgUrl(rune2);*/

        }
        rune.put("rune1", runeImgUrl1);
        rune.put("rune2", runeImgUrl2);

        return rune;
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
    public Item setItem(int itemNum) {

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

        try {
            //(item.json) itemResult값 parse해서 JsonObject로 받아오기 K:V
            HttpResponse<String> itemResultReponse = HttpConnUtil.sendHttpGetRequest(itemUrl);
            JsonObject itemResult = gson.fromJson(itemResultReponse.body(), JsonObject.class);
            //(item.json) Key값이 data 인 항목 { "data" : xx 부분 }
            JsonObject itemData = itemResult.getAsJsonObject("data");
            //(item.json) Key값이 data 안에서 1001인 항목 { "data" : {"1001" : xx 부분 }}
            JsonObject itemDtl = itemData.getAsJsonObject(String.valueOf(itemNum));

            String itemName = itemDtl.get("name").getAsString();
            String itemDesc = itemDtl.get("description").getAsString();
            String itemText = itemDtl.get("plaintext").getAsString();

            item.setItemTooltip("<b>" + itemName + "</b>" + "/n <hr>" + itemDesc + "<br>" + itemText);

        } catch (RimumuException e) {
            new RimumuException.NotFoundException("ITEM", e.getMessage());
        }

        return item;
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
    public Match setMatchDtls(Summoner summoner, String matchId) throws RimumuException {

        LOGGER.info("Match : {}", matchId);

        Match match = new Match();
        match.setMatchId(matchId);

        //matchData 중 info : xx 부분
        JsonObject info = getMatchIdInfo(matchId);
        if (isNotValid(info, "INFO")) {
            return match;
        }

        //게임종류(협곡 칼바람 등) //모드 추가 시 추가 필요
        match.setQueueId(getGameType(info.get("queueId").getAsString()));

        //게임시간
        long gameDuration = info.get("gameDuration").getAsLong();
        match.setGameDuration(DateTimeUtil.toDuration(gameDuration));
        long gameStarted = info.get("gameStartTimestamp").getAsLong() / 1000;
        match.setGamePlayedAt(DateTimeUtil.fromBetweenNow(gameStarted) + " 전");

        /*
         * participants 키의 배열['participants':{},] 가져오기(플레이어 당 인게임) // 블루 0~4/ 레드 5~9
         * 플레이어 수 만큼 도는 for문
         */
        JsonArray partiInArr = info.getAsJsonArray("participants");
        LOGGER.info("== Participants  사이즈 체크 : {}", partiInArr.size());

        List<Participant> participants = new ArrayList<>();

        for (JsonElement parti : partiInArr) {

            JsonObject inGame = parti.getAsJsonObject();
            Participant participant = new Participant();

            participant.setInName(inGame.get("summonerName").getAsString());
            participant.setPuuid(inGame.get("puuid").getAsString());

            // 챔프네임의 대소문자가 match Json과 img API가 동일하지 않은 이유로 에러발생. 때문에 emun에서 가져옴
            String champ = ChampionKey.valueOf("K" + inGame.get("championId").getAsString()).label();
            participant.setInChamp(champ);
            participant.setChampImgUrl(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/champion/" + champ + ".png");

            if(summoner.getPuuid().equals(participant.getPuuid())) {
                MyGame myGame = new MyGame();
                // participant가 나일 경우 추가 정보 세팅
                myGame.setInChamp(champ);
                myGame.setChampImgUrl(participant.getChampImgUrl());
                match.setMyGame(myGame);
                setGameDetail(match, inGame, summoner);
            }

            participants.add(participant);

        } // 1 matchId 종료
        match.setParticipants(participants);

        return match;
    }

    private void setGameDetail(Match match, JsonObject inGame, Summoner summoner) {

        // KDA
        int kill = inGame.get("kills").getAsInt();
        int death = inGame.get("deaths").getAsInt();
        int assists = inGame.get("assists").getAsInt();

        MyGame myGame = match.getMyGame();
        // 해당 판 KDA
        myGame.setKill(kill);
        myGame.setDeath(death);
        myGame.setAssist(assists);
        myGame.setAvg(getKdaAvg(kill, death, assists));

        // inGame 룬
        Map<String, String> runes = getRune(inGame);
        myGame.setRuneImgUrl1(runes.get("rune1"));
        myGame.setRuneImgUrl2(runes.get("rune2"));

        // inGame 스펠 [{"summonerId1:""}]
        Map<String, String> spells = getSpell(inGame);
        myGame.setSpImgUrl1(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/spell/" + spells.get("spell1") + ".png");
        myGame.setSpImgUrl2(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/spell/" + spells.get("spell2") + ".png");

        // inGame item 이미지 [{"item":xx}]
        List<Item> itemList = Stream.iterate(0, t -> t < 7, t -> t + 1)
                .map(t -> "item" + t)
                .map(inGame::get)
                .map(JsonElement::getAsInt)
                .map(this::setItem)
                .toList();
        myGame.setItemList(itemList);

        // 최근 전적
        SummonerRecent recent = summoner.getRecent();
        // 단일 경기 승리, 패배
        Boolean win = inGame.get("win").getAsBoolean();
        if (win) {
            match.setWin("WIN");
            match.setTable("table-primary");
            recent.setWin(recent.getWin() + 1);
        } else {
            match.setWin("LOSE");
            match.setTable("table-danger");
            recent.setLose(recent.getLose() + 1);
        }
        // 최근 전적 KDA
        recent.setKill(recent.getKill() + kill);
        recent.setDeath(recent.getDeath() + death);
        recent.setAssist(recent.getAssist() + assists);
        recent.setAvg(getKdaAvg(recent.getKill(), recent.getAssist(), recent.getDeath()));
        //return gameDetail;
    }

    /**
     * object != null -> 통과(false가 정상)
     *
     * @param object
     * @param item
     * @return boolean
     * @throws RimumuException.InvalidationException
     */
    private boolean isNotValid(JsonObject object, String item) throws RimumuException {
        if (ObjectUtils.isEmpty(object)) {
            throw new RimumuException.InvalidationException(item);
        }
        return false;
    }
}

