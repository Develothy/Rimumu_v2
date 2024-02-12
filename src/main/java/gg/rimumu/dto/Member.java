package gg.rimumu.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member {

    private Long id;
    private String email;
    private String nickname;
    private String password;
    private Social social;

    public enum Social {
        KAKAO("kakao"), NAVER("naver");

        private String label;

        Social(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

    }
}
