package gg.rimumu.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


@Getter
@Setter
@ToString
public class Match {

    private String matchId;

    private String queueId;

    private String gameDuration;

    private String gamePlayedAt;

    private String win;
    private String table;


    private List<Participant> Participants;
    private MyGame myGame;
}
