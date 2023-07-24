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
    public SummonerDto smnSearch(String smn, int offset) throws RimumuException{
        String url = RimumuKey.SUMMONER_INFO_URL + smn;

        HttpResponse<String> smnSearchResponse = HttpConnUtil.sendHttpGetRequest(url);
        String accResultStr;

        switch (smnSearchResponse.statusCode()) {
            case 200 -> accResultStr = smnSearchResponse.body();
            case 404 -> throw new RimumuException.SummonerNotFoundException(smn);
            default -> throw new RimumuException.ServerException();
        }

        // 검색 소환사 account 정보 가져오기
        SummonerDto summonerDto = gson.fromJson(accResultStr, SummonerDto.class);
        smnInfo(summonerDto, offset);

        return summonerDto;
    }

    //소환사 정보
    public SummonerDto smnInfo(SummonerDto summonerDto, int offset) throws RimumuException.MatchNotFoundException {

        // 아이콘 이미지 주소
        summonerDto.setIconImgUrl(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/profileicon/" + summonerDto.getProfileIconId() + ".png");

        // 티어 조회
        getTier(summonerDto);

        // 게임중 여부 조회 (riot developer api 막힘)
        currentGame(summonerDto);

        // matchId 최근 20게임
        matchesUrl(summonerDto, offset);


        // matchDtlList
        matchDtls(summonerDto);

        return summonerDto;
    } // smnInfo() 소환사 정보 종료


    // current 현재 게임 여부 ---------------
    public SummonerDto currentGame(SummonerDto summonerDto) {

        String curUrl = RimumuKey.SUMMONER_CURRENT_URL + summonerDto.getId();

        // 현재 게임 중일 경우 실행 //
        try {
            String curResultStr;

            HttpResponse<String> smnSearchResponse = HttpConnUtil.sendHttpGetRequest(curUrl);

            switch (smnSearchResponse.statusCode()) {
                case 200 -> curResultStr = smnSearchResponse.body();
                default -> {
                    LOGGER.info("Not playing now");
                    return summonerDto;
                }
            }

            JsonObject curResult = gson.fromJson(curResultStr, JsonObject.class);
            summonerDto.setCurrent(true);

            // 큐 타입
            String queueId = curResult.get("gameQueueConfigId").getAsString();
            summonerDto.setQueueId(getGameType(queueId));

            // participants : ['x','x'] 부분 arr
            JsonArray partiArr = curResult.getAsJsonArray("participants");

            for (JsonElement parti : partiArr) {

                // i번째 participant
                JsonObject inGame = parti.getAsJsonObject();
                //inGame participant(p)의 id == myId 비교
                String compareId = inGame.get("summonerId").getAsString();
                if (compareId.equals(summonerDto.getId())) {
                    String curChamp = ChampionKey.valueOf("K"+inGame.get("championId")).getLabel();
                    String curChampImg = RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/champion/" + curChamp +".png";
                    summonerDto.setCurChamp("현재 " + curChamp + " 게임중!");
                    summonerDto.setCurChampUrl(curChampImg);
                    return summonerDto;
                }
            }
        } catch (Exception e) {
            LOGGER.error("인게임 체크 중 오류 발생");
        }
        return summonerDto;
    }
    // current 현재 게임 여부 종료

    // 티어 조회 로직
    public SummonerDto getTier(SummonerDto summonerDto) {

        String rankUrl = RimumuKey.SUMMONER_TIER_URL + summonerDto.getId();
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
                    summonerDto.setSoloTier(ranks.get("tier").getAsString()); // 챌, 다이아, 플레 등
                    summonerDto.setSoloRank(ranks.get("rank").getAsString()); // 1 ~ 4
                    summonerDto.setSoloLeaguePoints(ranks.get("leaguePoints").getAsString()); // 티어 LP
                    summonerDto.setSoloWins(ranks.get("wins").getAsString()); //랭크 전체 승
                    summonerDto.setSoloLosses(ranks.get("losses").getAsString()); //랭크 전체 패
                }
                //자랭
                else if ("RANKED_FLEX_SR".equals(rankType)) {
                    summonerDto.setFlexTier(ranks.get("tier").getAsString());
                    summonerDto.setFlexRank(ranks.get("rank").getAsString());
                    summonerDto.setFlexLeaguePoints(ranks.get("leaguePoints").getAsString());
                    summonerDto.setFlexWins(ranks.get("wins").getAsString());
                    summonerDto.setFlexLosses(ranks.get("losses").getAsString());
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
    public SummonerDto matchesUrl(SummonerDto summonerDto, int offset) throws RimumuException.MatchNotFoundException {

        String matUrl = RimumuKey.SUMMONER_MATCHES_URL + summonerDto.getPuuid() + "/ids?start=" + offset + "&count20";
        HttpResponse<String> smnMatchResponse = HttpConnUtil.sendHttpGetRequest(matUrl);

        String matchesStr;

        switch (smnMatchResponse.statusCode()) {
            case 200 -> matchesStr = smnMatchResponse.body();
            default -> {
                LOGGER.error("Match List 정보를 찾을 수 없습니다.");
                throw new RimumuException.MatchNotFoundException(summonerDto.getName());
            }
        }
        List<SummonerDto> matchesArr = gson.fromJson(matchesStr, List.class);
        summonerDto.setMatchIdList(matchesArr);

        LOGGER.info("matchesUrl() matcheIdList : " + summonerDto.getMatchIdList().toString());
        return summonerDto;
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
    public ItemDto getItem(int itemNum) {

        ItemDto itemDto = new ItemDto();

        // item이 없는 칸 회색템 표시
        if (itemNum == 0) {
            itemDto.setItemNum(itemNum);
            itemDto.setItemImgUrl("/img/itemNull.png");
            itemDto.setItemTooltip("보이지 않는 검이 가장 무서운 법.....");
            return itemDto;
        }
        // inGame 나의 item 설명 (툴팁)
        // item.json URL 연결

        itemDto.setItemNum(itemNum);
        itemDto.setItemImgUrl(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/item/" + itemNum + ".png");

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

        itemDto.setItemTooltip("<b>" + itemName + "</b>" + "/n <hr>" + itemDesc + "<br>" + itemText);

        return itemDto;
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
    public SummonerDto matchDtls(SummonerDto summonerDto) throws RimumuException.MatchNotFoundException {

        List<SummonerDto> matchIdList = summonerDto.getMatchIdList();
        List<MatchDto> matchDtoList = new ArrayList<>();

        //매치 당 정보 가져오기 / 20게임 정보의 api 이용 중
        for (int i = 0; i < matchIdList.size() - 15; i++) {

            String matchId = "";
            matchId = String.valueOf(matchIdList.get(i));
            LOGGER.info("{}번 Match : {}", i, matchId);

            MatchDto matchDto = new MatchDto();
            matchDto.setMatchId(matchId);

            // i번째 matchId에 대한 정보

            //matchData 중 info : xx 부분
            JsonObject info = getMatchIdInfo(matchId);

            //게임종류(협곡 칼바람 등) //모드 추가 시 추가 필요
            matchDto.setQueueId(getGameType(info.get("queueId").getAsString()));

            //게임시간
            long gameDuration = info.get("gameDuration").getAsLong();
            matchDto.setGameDuration(DateTimeUtil.convertDuration(gameDuration));
            long gameStarted = info.get("gameStartTimestamp").getAsLong() / 1000;
            matchDto.setGamePlayedAt(DateTimeUtil.convertBetween(gameStarted) + " 전");

            LOGGER.info("== 게임시간 : {}", matchDto.getGameDuration());
            LOGGER.info("== {}", matchDto.getGamePlayedAt());

            /*
             * participants 키의 배열['participants':{},] 가져오기(플레이어 당 인게임) // 블루 0~4/ 레드 5~9
             * 플레이어 수 만큼 도는 for문
             */
            JsonArray partiInArr = info.getAsJsonArray("participants");
            LOGGER.info("== Participants  사이즈 체크 : {}", partiInArr.size());

            List<ParticipantDto> partiDtoList = new ArrayList<>();

            for (JsonElement parti : partiInArr) {

                JsonObject inGame = parti.getAsJsonObject();
                ParticipantDto partiDto = new ParticipantDto();

                List<String> nameAndChamp = getPartiNameAndChamp(inGame);
                partiDto.setInName(nameAndChamp.get(0));
                // 챔프네임의 대소문자가 match Json과 img API가 동일하지 않은 이유로 에러발생. 때문에 emun에서 가져옴
                String champ = ChampionKey.valueOf("K" + nameAndChamp.get(1)).label();
                partiDto.setInChamp(champ);
                partiDto.setChampImgUrl(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/champion/" + champ + ".png");

                // 해당 parti의 id가 검색된 id인지 비교
                if (summonerDto.getName().equals(partiDto.getInName())) {

                    // participant가 나일 경우 추가 정보 세팅
                    setMyGame(summonerDto, matchDto, inGame, champ);
                }

                partiDtoList.add(partiDto);

            } // 1 matchId 종료
            matchDto.setPartiDtoList(partiDtoList);
            matchDtoList.add(matchDto);
            summonerDto.setMatchDtoList(matchDtoList);
        }
        LOGGER.info("{} 조회 종료", summonerDto.getName());
        return summonerDto;
    }

    private void setMyGame(SummonerDto summonerDto, MatchDto matchDto, JsonObject inGame, String inChamp) {

        // 단일 경기 승리, 패배
        Boolean win = inGame.get("win").getAsBoolean();
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
        myGameDto.setMyChampUrl(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/champion/" + inChamp + ".png");

        // KDA
        int myK = inGame.get("kills").getAsInt();
        int myD = inGame.get("deaths").getAsInt();
        int myA = inGame.get("assists").getAsInt();

        // 해당 판 KDA
        myGameDto.setMyK(myK);
        myGameDto.setMyD(myD);
        myGameDto.setMyA(myA);
        myGameDto.setMyAvg(getKdaAvg(myK, myD, myA));
        LOGGER.info("== myAvg : {}", myGameDto.getMyAvg());
        // 최근 전적 KDA
        summonerDto.setRecentKill(summonerDto.getRecentKill()+myK);
        summonerDto.setRecentDeath(summonerDto.getRecentDeath()+myD);
        summonerDto.setRecentAssist(summonerDto.getRecentAssist()+myA);
        summonerDto.setRecentTotal(summonerDto.getRecentTotal()+1);
        summonerDto.setRecentAvg(getKdaAvg(summonerDto.getRecentKill(), summonerDto.getRecentAssist(), summonerDto.getRecentDeath()));

        // 나의 inGame 룬
        List<String> runes = getRune(inGame);
        myGameDto.setRuneImgUrl1(runes.get(0));
        myGameDto.setRuneImgUrl2(runes.get(1));

        // 나의 inGame 스펠 [{"summonerId1:""}]
        List<String> spells = getSpell(inGame);
        myGameDto.setSpImgUrl1(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/spell/" + spells.get(0) + ".png");
        myGameDto.setSpImgUrl2(RimumuKey.DD_URL + VersionUtil.DD_VERSION + "/img/spell/" + spells.get(1) + ".png");

        // 나의 inGame item 이미지 [{"item":xx}]
        List<ItemDto> itemList = Stream.iterate(0, t -> t < 7, t -> t + 1)
                .map(t -> "item" + t)
                .map(inGame::get)
                .map(JsonElement::getAsInt)
                .map(this::getItem)
                .toList();

        myGameDto.setItemDtoList(itemList);
        matchDto.setMyGameDto(myGameDto);
    }
}
