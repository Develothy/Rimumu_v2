package gg.rimumu.dto;

import gg.rimumu.common.util.DateTimeUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class Match {

    private String matchId;
    private int queueId;
    private String summonerPuuid;

    private String gameDuration;
    private String gameStartTimestamp;
    private String gamePlayedAt;

    private String win;
    private String table;

    private List<Participant> participants;
    private MyGame myGame;

    public void afterPropertiesSet() {
        setGamePlayedAt();
        for (Participant participant : participants) {
            participant.afterPropertiesSet();
        }
    }

    private void setGamePlayedAt() {
        this.gameDuration = DateTimeUtil.toDuration(Long.valueOf(this.gameDuration));
        this.gamePlayedAt = DateTimeUtil.fromBetweenNow(Long.valueOf(this.gameStartTimestamp)/1000) + " 전";
    }

    @Override
    public String toString() {
        return "Match{" +
                "matchId='" + matchId + '\'' +
                ", queueId=" + queueId +
                ", summonerPuuid='" + summonerPuuid + '\'' +
                ", gameDuration='" + gameDuration + '\'' +
                ", gameStartTimestamp='" + gameStartTimestamp + '\'' +
                ", gamePlayedAt='" + gamePlayedAt + '\'' +
                ", win='" + win + '\'' +
                ", table='" + table + '\'' +
                ", participants=" + participants.get(0).toString() +
                ", myGame=" + myGame +
                '}';
    }
}
