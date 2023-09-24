package gg.rimumu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SummonerRecent {
    private int win;
    private int lose;
    private int kill;
    private int death;
    private int assist;
    private int total;
    private String avg;
}
