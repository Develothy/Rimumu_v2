package gg.rimumu.common.audit.log;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    public void recordLog(AuditLog auditLog) {
        auditLog.setCreatedAt(new Date());
        log.info(auditLog.toString());
    }
}
