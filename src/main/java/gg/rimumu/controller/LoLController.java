package gg.rimumu.controller;

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

    @GetMapping("/version")
    public String version() {
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
