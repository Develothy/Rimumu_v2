package gg.rimumu.common;

import lombok.Getter;

@Getter
public enum SpellKey {

    SP1("SummonerBoost"),
    SP3("SummonerExhaust"),
    SP4("SummonerFlash"),
    SP6("SummonerHaste"),
    SP7("SummonerHeal"),
    SP11("SummonerSmite"),
    SP12("SummonerTeleport"),
    SP13("SummonerMana"),
    SP14("SummonerDot"),
    SP21("SummonerBarrier"),
    SP30("SummonerPoroRecall"),
    SP31("SummonerPoroThrow"),
    SP32("SummonerSnowball"),
    SP39("SummonerSnowURFSnowball_Mark"),
    SP54("Summoner_UltBookPlaceholder"),
    SP55("Summoner_UltBookSmitePlaceholder"),
    SP2201("SummonerCherryHold"),
    SP2202("SummonerCherryFlash");


    private final String label;

    SpellKey(String label) {this.label = label;}
    public String label() {
        return label;
    }

}
