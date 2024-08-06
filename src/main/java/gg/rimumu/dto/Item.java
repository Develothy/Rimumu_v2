package gg.rimumu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Item {

    private int num;
    private String name;
    private String description;
    private int gold;

    Item(int num) {
        this.num = num;
    }

}
