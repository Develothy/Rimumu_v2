package gg.rimumu.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MatchDetail {

    private String killRed;
    private String killBlue;

    private String deathRed;
    private String deathBlue;

    private String assistRed;
    private String assistBlue;

    private String towerRed;
    private String towerBlue;

    private String dragonRed;
    private String dragonBlue;

    private String baronRed;
    private String baronBlue;

    private List<Participant> participants;

}
