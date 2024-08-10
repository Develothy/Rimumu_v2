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
    private String gameName;
    private String tagLine;
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

    private List<Match> matchList = new ArrayList<>();

    public void setRank(SummonerRank rank) {
        switch (rank.getQueueType()) {
            case RANKED_SOLO_5x5 -> {
                this.soloTier = rank.getTier();
                this.soloRank = rank.getRank();
                this.soloLeaguePoints = rank.getLeaguePoints();
                this.soloWins = rank.getWins();
                this.soloLosses = rank.getLosses();
            }
            case RANKED_FLEX_SR -> {
                this.flexTier = rank.getTier();
                this.flexRank = rank.getRank();
                this.flexLeaguePoints = rank.getLeaguePoints();
                this.flexWins = rank.getWins();
                this.flexLosses = rank.getLosses();
            }
        }
    }


    public Summoner toResponse() {
        return Summoner.builder()
                .gameName(this.gameName)
                .tagLine(this.tagLine)
                .summonerLevel(this.summonerLevel)
                .masteryChamp(this.masteryChamp)
                .profileIconId(this.profileIconId)
                .soloTier(this.soloTier)
                .soloRank(this.soloRank)
                .soloLeaguePoints(this.soloLeaguePoints)
                .soloWins(this.soloWins)
                .flexTier(this.flexTier)
                .flexRank(this.flexRank)
                .flexWins(this.flexWins)
                .flexLosses(this.flexLosses)
                .isCurrent(this.isCurrent)
                .queueId(this.queueId)
                .curChamp(this.curChamp)
                .recent(this.recent)
                .matchList(this.matchList)
                .build();
    }
}
