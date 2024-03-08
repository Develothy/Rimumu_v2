package gg.rimumu.controller;

import gg.rimumu.common.audit.aspect.audit.Audit;
import gg.rimumu.common.audit.aspect.audit.AuditAction;
import gg.rimumu.common.audit.aspect.audit.AuditExclude;
import gg.rimumu.common.audit.common.Actor;
import gg.rimumu.common.key.ChampionKey;
import gg.rimumu.common.util.ApplicationDataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/lol")
@RestController
@RequiredArgsConstructor
public class LoLController {

    private final ApplicationDataUtil applicationDataUtil;

    @Audit(target = "version", action = AuditAction.CREATED)
    @GetMapping("/version")
    public String version(@RequestBody Actor actor) {
        return ApplicationDataUtil.DD_VERSION;
    }

    @PutMapping("/version")
    public String init() {
        return applicationDataUtil.InitVersion();
    }
    @PostMapping("/version")
    public String set(@RequestParam String version) {
        return applicationDataUtil.serVersion(version);
    }

    @GetMapping("/champion")
    public Map list() {
        return ChampionKey.valuesWithLabel();
    }

}
