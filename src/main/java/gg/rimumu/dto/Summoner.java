package gg.rimumu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.*;


@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Summoner {

    private String id;
    private String puuid;
    private String name;
    private int summonerLevel;
    private String masteryChamp = "Amumu";
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


    public Summoner toResponse() {
        return Summoner.builder()
                .name(name)
                .summonerLevel(summonerLevel)
                .masteryChamp(masteryChamp)
                .profileIconId(profileIconId)
                .soloTier(soloTier)
                .soloRank(soloRank)
                .soloLeaguePoints(soloLeaguePoints)
                .soloWins(soloWins)
                .flexTier(flexTier)
                .flexRank(flexRank)
                .flexWins(flexWins)
                .flexLosses(flexLosses)
                .isCurrent(isCurrent)
                .queueId(queueId)
                .curChamp(curChamp)
                .recent(recent)
                .matchList(matchList)
                .build();
    }
}
