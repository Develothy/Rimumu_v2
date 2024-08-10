package gg.rimumu.dto;

import lombok.Data;

@Data
public class SummonerRank {

    private QueueType queueType;
    private String tier;
    private String rank;
    private String leaguePoints;
    private String wins;
    private String losses;

    enum QueueType {
        RANKED_SOLO_5x5("soloRank"),
        RANKED_FLEX_SR("flexRank");

        private String label;

        QueueType(String label) {
            this.label = label;
        }
        public String label() {
            return label;
        }
    }
}