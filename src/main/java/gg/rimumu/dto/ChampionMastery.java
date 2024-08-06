package gg.rimumu.dto;

import lombok.Data;

@Data
public class ChampionMastery {

    private String puuid;
    private String championId;
    private int championLevel;
    private int championPoints;
    private int championSeasonMilestone;

}