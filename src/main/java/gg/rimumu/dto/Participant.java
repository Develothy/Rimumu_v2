package gg.rimumu.dto;

import gg.rimumu.common.key.ChampionKey;
import gg.rimumu.common.key.SpellKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@SuperBuilder
public class Participant {

    private String summonerName;
    private String riotIdTagline;
    private String puuid;


    private String championName;
    private String championId;
    private int champLevel;


    private boolean win;
    private int kills;
    private int deaths;
    private int assists;
    private String avg;

    private int item0;
    private int item1;
    private int item2;
    private int item3;
    private int item4;
    private int item5;
    private int item6;

    // rune
    private Perks perks;
    private String rune1;
    private String rune2;

    // spell
    private String summoner1Id;
    private String summoner2Id;


    private int totalDamageDealtToChampions;
    private int totalDamageTaken;
    private int totalMinionsKilled;


    public void afterPropertiesSet() {
        setChampionName();
        setAvg();
        setRune1();
        setRune2();
        setSpell();
    }

    private void setChampionName() {
        this.championName = ChampionKey.valueOf("K" + this.championId).label();
    }

    private void setAvg() {
        if (this.deaths == 0) {
            this.avg = "Perfect!";
        } else {
            this.avg = String.format("%.2f", (this.kills + this.assists) / (double) this.deaths);
        }
    }

    @Getter @Setter
    public class Perks {
        private List<PerkStyle> styles = new ArrayList<>();

        @Getter @Setter
        public class PerkStyle {
            private int style;

        }

    }

    private void setRune1() {
        this.rune1 = getRune(this.perks.styles.get(0).getStyle());
    }
    private void setRune2() {
        this.rune2 = getRune(this.perks.styles.get(1).getStyle());
    }

    private String getRune(int Style) {

        String rune = null;
        switch (Style) {
            case 8000 -> rune = "7201_Precision";
            case 8100 -> rune = "7200_Domination";
            case 8200 -> rune = "7202_Sorcery";
            case 8300 -> rune = "7203_Whimsy";
            case 8400 -> rune = "7204_Resolve";
        }
        return rune;
    }

    private void setSpell() {
        this.summoner1Id = SpellKey.valueOf("SP" + this.summoner1Id).label();
        this.summoner2Id = SpellKey.valueOf("SP" + this.summoner2Id).label();
    }

}
