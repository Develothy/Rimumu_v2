package gg.rimumu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;


@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Summoner {

    private String id;
    private String puuid;
    private String name;
    private int summonerLevel;
    // 아이콘 이미지 주소
    private int profileIconId;

    // 솔랭 기록
    private String soloTier = "Unranked";
    private String soloRank = null;
    private String soloLeaguePoints = null;
    private String soloWins = null;
    private String soloLosses = null;

    // 자유랭크 기록
    private String flexTier = "Unranked";
    private String flexRank = null;
    private String flexLeaguePoints = null;
    private String flexWins = null;
    private String flexLosses = null;

    // 현재 게임중인 기록 (current)
    private boolean isCurrent = false; // 게임 여부 true false
    private String queueId = null;
    private String curChamp = null; // champ

    private SummonerRecent recent = new SummonerRecent();

    private List<Match> matchList = null;


    public SummonerResponse toResponse() {
        SummonerResponse response = new SummonerResponse();
        response.setName(this.name);
        response.setSummonerLevel(this.summonerLevel);
        response.setProfileIconId(this.profileIconId);
        response.setSoloTier(this.soloTier);
        response.setSoloRank(this.soloRank);
        response.setSoloLeaguePoints(this.soloLeaguePoints);
        response.setSoloWins(this.soloWins);
        response.setSoloLosses(this.soloLosses);
        response.setFlexRank(this.flexRank);
        response.setFlexLeaguePoints(this.flexLeaguePoints);
        response.setFlexWins(this.flexWins);
        response.setFlexLosses(this.flexLosses);
        response.setCurrent(this.isCurrent);
        response.setQueueId(this.queueId);
        response.setCurChamp(this.curChamp);
        response.setRecent(this.recent);
        response.setMatchList(this.matchList);

        return response;
    }
}
