package gg.rimumu.common.audit.common;

import lombok.*;

@Data
@Setter @Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Actor {

    private String username;

    private ClientIp clientIp;

}
