package gg.rimumu.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GameDetail {

    // myChamp
    private String inChamp;

    // 닉네임?
    private String inName;
    private String puuid;

    //myK, partiK
    private int kill;

    // myD, partiD
    private int death;

    // myA, partiA
    private int assist;

    // myAvg, partiAvg
    private String avg;

    private String rune1;
    private String rune2;

    private String spell1;
    private String spell2;

    private List<Item> itemList = new ArrayList<>();

    private int damage;
    private int takenDamage;
    private int minions;

}
