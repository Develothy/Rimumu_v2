package gg.rimumu.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ItemDto {

    private int itemNum;

    private String itemName;

    private String itemImgUrl;

    // description
    private String itemDesc;

    // plaintext
    private String itemText;

    private String itemTooltip;
}
