package gg.rimumu.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

import java.util.ArrayList;

@Getter
@Setter
//@ToString
public class SummonerDto {

    private String id;
    private String puuid;

    private String name;
    // 레벨
    private int smLv;
    // 아이콘 이미지 주소
    private String imgIconURL;

    // 솔랭 기록
    private String soloTier;
    private String soloRank;
    private String soloLeaguePoints;
    private String soloWins;
    private String soloLosses;

    // 자유랭크 기록
    private String flexTier;
    private String flexRank;
    private String flexLeaguePoints;
    private String flexWins;
    private String flexLosses;

    // 현재 게임중인 기록 (current)
    private String queueId;
    private String current; // 게임 여부 true false
    private String curChampUrl; // chap 이미지

    // 검색한 소환사 최근 전적 승률 KDA
    private int recentWin;
    private int recentLose;
    private int recentKill;
    private int recentDeath;
    private int recentAssist;

    // matchId list
    private List<SummonerDto> matchIdList = new ArrayList<>();




}
