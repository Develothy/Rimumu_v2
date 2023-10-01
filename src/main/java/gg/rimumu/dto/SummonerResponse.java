package gg.rimumu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import gg.rimumu.util.ApplicationDataUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SummonerResponse extends Summoner {

    private String version = ApplicationDataUtil.DD_VERSION;
}
