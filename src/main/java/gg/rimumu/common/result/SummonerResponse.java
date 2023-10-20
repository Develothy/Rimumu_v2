package gg.rimumu.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import gg.rimumu.common.util.ApplicationDataUtil;
import gg.rimumu.dto.Summoner;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SummonerResponse extends Summoner {

    private String version = ApplicationDataUtil.DD_VERSION;
}
