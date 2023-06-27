package gg.rimumu.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@ToString
public class MatchDto {

    private String matchId;

    private String queueId;

    private String gameDuration;

    private String gamePlayedAt;

    private String win;
    private String table;


    private List<ParticipantDto> partiDtoList;
    private MyGameDto myGameDto;
}
