package gg.rimumu.common.audit.log;

import gg.rimumu.common.audit.aspect.audit.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    private Long id;

    private String target;

    private String actor;

    private AuditAction action;

    private String signature;

    private String parameters;

    private String ip;

    private Date createdAt;
}
