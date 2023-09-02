package gg.rimumu.common;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter

public class RimumuResult<T> implements Serializable {

    private int code = 0;

    private String description = "정상 처리 되었습니다.";

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private T data;

    public RimumuResult(Object data) {
        this.data = (T) data;
    }

}
