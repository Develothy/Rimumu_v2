package gg.rimumu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;


@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Summoner {

    private String id;
    private String puuid;
    private String name;
    private int summonerLevel;
    // 아이콘 이미지 주소
    private int profileIconId;
    private String iconImgUrl;

    // 솔랭 기록
    private String soloTier = "Unranked";
    private String soloRank = null;
    private String soloLeaguePoints = null;
    private String soloWins = null;
    private String soloLosses = null;

    // 자유랭크 기록
    private String flexTier = "Unranked";
    private String flexRank = null;
    private String flexLeaguePoints = null;
    private String flexWins = null;
    private String flexLosses = null;

    // 현재 게임중인 기록 (current)
    private boolean isCurrent = false; // 게임 여부 true false
    private String queueId = null;
    private String curChamp = null; // champ
    private String curChampUrl = null; // champ 이미지

    private SummonerRecent recent = new SummonerRecent();

    private List<Match> matchList = null;

}
