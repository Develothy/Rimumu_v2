package gg.rimumu.dto;

import lombok.Data;

@Data
public class CurrentGameInfo {

    private Long gameId;
    private String gameType;
    private Long gameStartTime;
    private Long gameLength;
    private Long mapId;
    private String gameMode;
    private Long gameQueueConfigId;

}