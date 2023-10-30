package gg.rimumu.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import gg.rimumu.common.util.ApplicationDataUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemResponse {

    private String version = ApplicationDataUtil.DD_VERSION;

    private int num;
    private String name;
    private String description;
    private int gold;


}
