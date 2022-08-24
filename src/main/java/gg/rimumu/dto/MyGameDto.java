package gg.rimumu.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class MyGameDto {

    private String myChamp;

    private String myChampUrl;

    private int myK;
    private int myD;
    private int myA;

    private String myAvg;

    private String rune1;
    private String rune2;

    private String spell1;
    private String spell2;
    private String spImgUrl1;
    private String spImgUrl2;


    private List<MyGameDto> itemList;

}