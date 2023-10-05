package gg.rimumu.common;

import lombok.Getter;

@Getter
public enum GameTypeKey {
    
    T420("솔랭"),
    T430("일반"),
    T440("자유랭크"),
    T450("칼바람"),
    T70("단일 챔피언"),
    T300("포로왕"),
    T830("입문"),
    T840("초급"),
    T850("중급"),
    T900("무작위 우르프"),
    T1020("단일챔피언"),
    T1300("돌격 넥서스"),
    T1400("궁극기 주문서"),
    T1700("아레나"),
    T1900("우르프"),
    T2000("튜토리얼 1"),
    T2010("튜토리얼 2"),
    T2020("튜토리얼 3");

    private final String label;

    GameTypeKey(String label) {this.label = label;}
    public String label() {
        return label;
    }


}
