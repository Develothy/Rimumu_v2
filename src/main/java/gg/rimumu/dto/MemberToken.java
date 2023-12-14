package gg.rimumu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberToken {

    private String accessToken;
    private String refreshToken;

}
