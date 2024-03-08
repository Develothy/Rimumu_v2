package gg.rimumu.common.audit.aspect;
import gg.rimumu.common.audit.aspect.audit.Audit;
import gg.rimumu.common.audit.aspect.audit.AuditAction;
import gg.rimumu.common.audit.aspect.audit.AuditExclude;
import gg.rimumu.common.audit.common.Actor;
import gg.rimumu.common.audit.log.AuditLog;
import gg.rimumu.common.audit.log.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static gg.rimumu.common.audit.aspect.JoinPointUtil.*;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private final AuditLogService auditLogService;

    @AfterReturning("@annotation(auditAnnotation) && @annotation(gg.rimumu.common.audit.aspect.audit.Audit)")
    public void audit(JoinPoint joinPoint, Audit auditAnnotation) {
        String target = auditAnnotation.target();
        String fullTargetClassName = auditAnnotation.targetClass().toString();
        String targetClassName = fullTargetClassName.substring(fullTargetClassName.lastIndexOf(".") + 1);

        if (StringUtils.isEmpty(target) && "Object".equals(targetClassName)) {
            return;
        }
        if (!"Object".equals(targetClassName)) {
            target = targetClassName;
        }

        AuditAction action = auditAnnotation.action();
        if (action == AuditAction.NONE) {
            return;
        }

        Actor actor = extractActor(joinPoint);

        AuditLog auditLog = AuditLog.builder()
                .target(target)
                .action(action)
                .actor(actor.getUsername())
                .signature(extractSignature(joinPoint, AuditExclude.class))
                .parameters(extractParameters(joinPoint, AuditExclude.class))
                .ip(actor.getClientIp().getIp())
                .build();

        try {
            // log 남길 행위
            auditLogService.recordLog(auditLog);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
