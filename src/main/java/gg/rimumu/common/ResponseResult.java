package gg.rimumu.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResponseResult {

    private int code;
    private String body;

    public ResponseResult(int code, String body) {
        this.code = code;
        this.body = body;
    }
}
