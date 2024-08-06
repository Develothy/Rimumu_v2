package gg.rimumu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import gg.rimumu.service.SummonerService;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyGame extends Participant {

    private String performance;
    private List<Item> itemList;

    public static MyGame of(Participant p) {

        return MyGame.builder()
                .summonerName(p.getSummonerName())
                .riotIdTagline(p.getRiotIdTagline())
                .championName(p.getChampionName())
                .champLevel(p.getChampLevel())
                .win(p.isWin())
                .kills(p.getKills())
                .deaths(p.getDeaths())
                .assists(p.getAssists())
                .avg(p.getAvg())
                .rune1(p.getRune1())
                .rune2(p.getRune2())
                .summoner1Id(p.getSummoner1Id())
                .summoner2Id(p.getSummoner2Id())
                .itemList(setItemList(p.getItem0(), p.getItem2(), p.getItem3(), p.getItem4(), p.getItem5(), p.getItem6()))
                .build();
    }

    protected static List<Item> setItemList(int... items) {

        return Arrays.stream(items)
                .mapToObj(SummonerService::setItemDescription)
                .collect(Collectors.toList());

    }
}
