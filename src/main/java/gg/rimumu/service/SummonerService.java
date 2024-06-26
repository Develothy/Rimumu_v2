package gg.rimumu.service;

import com.google.gson.*;
import gg.rimumu.cache.CacheService;
import gg.rimumu.common.key.ChampionKey;
import gg.rimumu.common.key.GameTypeKey;
import gg.rimumu.common.key.RimumuKey;
import gg.rimumu.common.key.SpellKey;
import gg.rimumu.dto.*;
import gg.rimumu.exception.RimumuException;
import gg.rimumu.common.util.DateTimeUtil;
import gg.rimumu.common.util.HttpConnUtil;
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
    private CacheService cached;


    // 소환사 검색
    public Summoner smnSearch(String smn) throws RimumuException {

        Summoner summoner = getSummoner(smn);
        setSmnInfo(summoner);

        return summoner;
    }

    public Summoner getSummoner(String smn) throws RimumuException {
        String url = RimumuKey.SUMMONER_INFO_URL + smn;

        try {
            HttpResponse<String> smnSearchResponse = HttpConnUtil.sendHttpGetRequest(url);

            if (smnSearchResponse.statusCode() != 200) {
                throw new RimumuException.SummonerNotFoundException(smn);
            }

            return gson.fromJson(smnSearchResponse.body(), Summoner.class);

        } catch (RimumuException | JsonSyntaxException e) {
            LOGGER.error("!! getSummoner smnSearch error: {} / {}", smn, e.getMessage());
            throw new RimumuException.SummonerNotFoundException(smn);
        }
    }

    //소환사 정보
    public void setSmnInfo(Summoner summoner){

        // 티어 조회
        setTier(summoner);
        //setMasteryChamp(summoner);
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


    // 티어 조회 로직
    public void setTier(Summoner summoner) {

        String rankUrl = RimumuKey.SUMMONER_TIER_URL + summoner.getId();
        String rankResultStr = null;

        try {
            HttpResponse<String> rankResultResponse = HttpConnUtil.sendHttpGetRequest(rankUrl);
            rankResultStr = rankResultResponse.body();

        } catch (RimumuException e) {
            LOGGER.error("!! getTier rankResultResponse error", e.getMessage());
        }

        //언랭일 경우 [] 값
        if (ObjectUtils.isEmpty(rankResultStr)) {
            return;
        }

        try {
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
            // 랭크 정보 등록 종료
        } catch (Exception e) {
            LOGGER.error("!! getTier error. summoner id : {}", summoner.getId());
        }
    } // smnTier() 티어 죄회 로직 종료

    public void setMasteryChamp(Summoner summoner) {

        String masteryChampUrl = RimumuKey.SUMMONER_MASTERY_URL + summoner.getId() + "/top?count=1";

        try {
            LOGGER.info("summoner Id check : {}", summoner.getId() );
            HttpResponse<String> smnMasteryResponse = HttpConnUtil.sendHttpGetRequest(masteryChampUrl, false);
            JsonObject matchResult = JsonParser.parseString(smnMasteryResponse.body()).getAsJsonArray().get(0).getAsJsonObject();
            String masteryChamp = ChampionKey.valueOf("K" + matchResult.get("championId").getAsString()).getLabel();
            summoner.setMasteryChamp(masteryChamp);

        } catch (RimumuException | IllegalStateException e) {
            LOGGER.warn(masteryChampUrl);
            LOGGER.warn("!! get mastery champion error : {}", e.getMessage());
        }
    }

    // current 현재 게임 여부 ---------------
    public Summoner checkCurrentGame(Summoner summoner) {

        String curUrl = RimumuKey.SUMMONER_CURRENT_URL + summoner.getId();

        try {
            HttpResponse<String> smnSearchResponse = HttpConnUtil.sendHttpGetRequest(curUrl, false);

            // 현재 게임 중 아님 //
            if (200 != smnSearchResponse.statusCode()) {
                LOGGER.info("Not playing now");
                return summoner;
            }

            JsonObject curResult = gson.fromJson(smnSearchResponse.body(), JsonObject.class);
            summoner.setCurrent(true);

            // 큐 타입
            String queueId = curResult.get("gameQueueConfigId").getAsString();
            summoner.setQueueId(getGameType(queueId));

            // participants : ['x','x'] 부분 arr
            JsonArray partiArr = curResult.getAsJsonArray("participants");

            for (JsonElement parti : partiArr) {
                // i번째 participant
                JsonObject inGame = parti.getAsJsonObject();
                String summonerId = inGame.get("summonerId").getAsString();

                //inGame participant(p)의 id == myId 비교
                if (summoner.getId().equals(summonerId)) {
                    String curChamp = cached.getChampionName(inGame.get("championId").getAsInt());
                    summoner.setCurChamp(curChamp);
                    return summoner;
                }
            }
        } catch (RimumuException | JsonSyntaxException e) {
            LOGGER.error("!! checkCurrentGame error. summoner id : {}", summoner.getId());
        }

        return summoner;
    }
    // current 현재 게임 여부 종료


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

            if (smnMatchResponse.statusCode() == 200) {
                return gson.fromJson(smnMatchResponse.body(), List.class);
            }

        } catch (RimumuException | JsonSyntaxException e) {
            LOGGER.error("!! getMatchesUrl error : {}", userPuuid);
        }

        throw new RimumuException.MatchNotFoundException(userPuuid);
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

        } catch (RimumuException | JsonSyntaxException e) {
            LOGGER.error("!! getMatchIdInfo error. match id : {}", matchId);
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

        String rune1 = ((JsonObject) styles.get(0)).get("style").getAsString();
        String rune2 = ((JsonObject) styles.get(1)).get("style").getAsString();

        rune.put("rune1", setRuneName(rune1));
        rune.put("rune2", setRuneName(rune2));

        return rune;
    }

    // rune 이미지 주소 변환
    public String setRuneName(String rune) {

        switch (rune) {
            case "8000" -> rune = "7201_Precision";
            case "8100" -> rune = "7200_Domination";
            case "8200" -> rune = "7202_Sorcery";
            case "8300" -> rune = "7203_Whimsy";
            case "8400" -> rune = "7204_Resolve";
        }
        return rune;
    }

    // item 구하기
    public Item setItem(int itemNum) {

        Item item = new Item();

        // item이 없는 칸 회색템 표시
        if (itemNum == 0) {
            item.setDescription("보이지 않는 검이 가장 무서운 법.....");
            return item;
        } else {
            item.setNum(itemNum);
        }

        // item TOOLTIP 템 정보
        try {
            JsonObject itemDtl = cached.getItem(itemNum);

            StringBuilder tooltip = new StringBuilder();
            tooltip.append("<b>");
            tooltip.append(itemDtl.get("name").getAsString());
            tooltip.append("</b>/n <hr>");
            tooltip.append(itemDtl.get("description").getAsString());
            tooltip.append("<br>");
            tooltip.append(itemDtl.get("plaintext").getAsString());

            item.setDescription(tooltip.toString());

        } catch (RimumuException e) {
            LOGGER.error("!! setItem error");
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

        List<Participant> participants = new ArrayList<>();

        for (JsonElement parti : partiInArr) {

            JsonObject inGame = parti.getAsJsonObject();
            Participant participant = new Participant();

            participant.setInName(inGame.get("summonerName").getAsString());
            participant.setPuuid(inGame.get("puuid").getAsString());

            // 챔프네임의 대소문자가 match Json과 img API가 동일하지 않은 이유로 에러발생. 때문에 emun에서 가져옴
            String champ = ChampionKey.valueOf("K" + inGame.get("championId").getAsString()).label();
            participant.setInChamp(champ);

            if(summoner.getPuuid().equals(participant.getPuuid())) {
                MyGame myGame = new MyGame();
                // participant가 나일 경우 추가 정보 세팅
                myGame.setInChamp(champ);
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
        myGame.setRune1(runes.get("rune1"));
        myGame.setRune2(runes.get("rune2"));

        // inGame 스펠 [{"summonerId1:""}]
        Map<String, String> spells = getSpell(inGame);
        myGame.setSpell1(spells.get("spell1"));
        myGame.setSpell2(spells.get("spell2"));

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

