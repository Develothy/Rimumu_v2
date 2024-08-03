package gg.rimumu.service;

import com.google.gson.*;
import gg.rimumu.cache.CacheService;
import gg.rimumu.common.key.ChampionKey;
import gg.rimumu.common.key.GameTypeKey;
import gg.rimumu.common.key.RimumuKey;
import gg.rimumu.common.key.SpellKey;
import gg.rimumu.dto.*;
import gg.rimumu.exception.RimumuException;
import gg.rimumu.common.util.HttpConnUtil;
import gg.rimumu.service.excutor.SummonerApiExecutor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SummonerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SummonerService.class);
    private static final Gson gson = new Gson();

    private final SummonerApiExecutor executor;

    private CacheService cached;


    // 소환사 검색
    public Summoner smnSearch(String smn, String tagline) throws RimumuException {

        Summoner summoner = getSummoner(smn, tagline);
        setSmnInfo(summoner);

        return summoner;
    }

    public String getPuuid(String smn, String tagline) throws RimumuException {

        try {
            String puuidUrl = RimumuKey.SUMMONER_PUUID_URL + smn + "/" + tagline;
            HttpResponse<String> puuidResponse = HttpConnUtil.sendHttpGetRequest(puuidUrl);
            if (puuidResponse.statusCode() != 200) {
                throw new RimumuException.SummonerNotFoundException(smn);
            }
            return gson.fromJson(puuidResponse.body(), Summoner.class).getPuuid();

        } catch (RimumuException | JsonSyntaxException e) {
            LOGGER.error("!! getPuuid smnSearch error: {} / {}", smn, e.getMessage());
            throw new RimumuException.SummonerNotFoundException(smn);
        }
    }

    public Summoner getSummoner(String smn, String tagline) throws RimumuException {
        Summoner summoner;

        try {
            String puuid = getPuuid(smn, tagline);

            String url = RimumuKey.SUMMONER_INFO_URL + puuid;
            HttpResponse<String> smnSearchResponse = HttpConnUtil.sendHttpGetRequest(url);
            if (smnSearchResponse.statusCode() != 200) {
                throw new RimumuException.SummonerNotFoundException(smn);
            }
            summoner = gson.fromJson(smnSearchResponse.body(), Summoner.class);

            return summoner;

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
        String puuid = summoner.getPuuid();
        String matchesUrl = RimumuKey.SUMMONER_MATCHES_URL + puuid + "/ids?start=" + offset + "&count=10";
        HttpResponse<String> smnMatchResponse = null;

        try {
            smnMatchResponse = HttpConnUtil.sendHttpGetRequest(matchesUrl);

            if (smnMatchResponse.statusCode() != 200) {
                throw new RimumuException.MatchNotFoundException(matchesUrl + "\n" + smnMatchResponse.body());
            }

            return gson.fromJson(smnMatchResponse.body(), List.class);

        } catch (RimumuException | JsonSyntaxException e) {
            LOGGER.error("!! getMatchesUrl error : {}\n {}", matchesUrl, e.getMessage());
            throw new RimumuException.MatchNotFoundException(matchesUrl + "\n" + e.getMessage());
        }
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


    /*
     * forEach 반복문 시작구간
     * 설명 : 챔피언, 게입타입, 승패, 게임 시간, KDA, 룬, 스펠, 아이템, 플레이어
     */
    public Match setMatchDtls(Summoner summoner, Match match) throws RimumuException {

        //matchData 중 info : xx 부분
        if (!isValid(match, "INFO")) {
            return match;
        }

        //게임종류(협곡 칼바람 등) //모드 추가 시 추가 필요
        System.out.println("info queueId;" + match.getQueueId());
        System.out.println("info;" + match.toString());

        /*
         * participants 키의 배열['participants':{},] 가져오기(플레이어 당 인게임) // 블루 0~4/ 레드 5~9
         * 플레이어 수 만큼 도는 for문
         */

        for (Participant parti : match.getParticipants()) {

            if(summoner.getPuuid().equals(parti.getPuuid())) {
                // participant가 나일 경우 추가 정보 세팅
                MyGame myGame = parti.of();
                match.setMyGame(myGame);
                setGameDetail(match, summoner);
                System.out.println("for " + match.getParticipants().get(0).toString());
            }

        } // 1 matchId 종료
        return match;
    }

    // 최근 전적만 구하는 것으로 바꿔야 할듯.나머지는 파싱에서
    private void setGameDetail(Match match, Summoner summoner) {

        MyGame myGame = match.getMyGame();

        // 최근 전적
        SummonerRecent recent = summoner.getRecent();
        // 단일 경기 승리, 패배
        if (myGame.isWin()) {
            match.setWin("WIN");
            match.setTable("table-primary");
            recent.setWin(recent.getWin() + 1);
        } else {
            match.setWin("LOSE");
            match.setTable("table-danger");
            recent.setLose(recent.getLose() + 1);
        }
        // 최근 전적 KDA
        recent.setKill(recent.getKill() + myGame.getKills());
        recent.setDeath(recent.getDeath() + myGame.getDeaths());
        recent.setAssist(recent.getAssist() + myGame.getAssists());
        recent.setAvg(myGame.getAvg());
    }

    /**
     * object != null -> exception(true가 정상)
     *
     * @param object
     * @param item
     * @return boolean
     * @throws RimumuException.InvalidationException
     */
    private boolean isValid(Object object, String item) throws RimumuException {
        if (ObjectUtils.isEmpty(object)) {
            throw new RimumuException.InvalidationException(item);
        }
        return true;
    }

    public List<Match> getMatchResult(Summoner summoner, int offset) throws RimumuException, ExecutionException, InterruptedException {

        List<String> matchIds = getMatches(summoner, offset);
        List<Match> targets = (List<Match>) executor.apiParallelCalls(summoner, matchIds);
        List<Match> matches = targets.stream()
                .map(m -> {
                    try {
                        return setMatchDtls(summoner, m);
                    } catch (RimumuException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(Collectors.toList());

        return matches;
    }
}

