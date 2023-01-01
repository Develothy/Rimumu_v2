package gg.rimumu.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class PartiDetailDto {

    private String champImgUrl;

    private String inChamp;

    private String inName;

    private int partiK;
    private int partiD;
    private int partiA;

    private String partiAvg;

    private String runeImgUrl1;
    private String runeImgUrl2;

    private String spImgUrl1;
    private String spImgUrl2;

    private int partiDamage;
    private int partiTakenDamage;
    private int partiMinions;

    private List<ItemDto> itemDtoList = new ArrayList<>();

}
