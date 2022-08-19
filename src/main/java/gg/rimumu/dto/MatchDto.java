package gg.rimumu.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class MatchDto {

    private String matchId;

    private String QueueId;

    private String gameDuration;


    private List<MatchDto> matchDtoList;
}
