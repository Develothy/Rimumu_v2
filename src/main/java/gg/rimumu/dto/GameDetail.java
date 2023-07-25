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

    // myChampUrl
    private String champImgUrl;

    // 닉네임?
    private String inName;

    //myK, partiK
    private int kill;

    // myD, partiD
    private int death;

    // myA, partiA
    private int assist;

    // myAvg, partiAvg
    private String avg;

    private String runeImgUrl1;
    private String runeImgUrl2;

    private String spImgUrl1;
    private String spImgUrl2;

    private List<Item> itemList = new ArrayList<>();

    private int damage;
    private int takenDamage;
    private int minions;

}
