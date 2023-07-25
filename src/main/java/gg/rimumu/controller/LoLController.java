package gg.rimumu.controller;

import gg.rimumu.common.ChampionKey;
import gg.rimumu.util.VersionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/lol")
@RestController
@RequiredArgsConstructor
public class LoLController {

    private final VersionUtil versionUtil;

    @GetMapping("/version")
    public String version() {
        return VersionUtil.DD_VERSION;
    }

    @PutMapping("/version")
    public String init() {
        return versionUtil.versionInit();
    }
    @PostMapping("/version")
    public String set(@RequestParam String version) {
        return versionUtil.versionSet(version);
    }

    @GetMapping("/champion")
    public Map list() {
        return ChampionKey.valuesWithLabel();
    }

}
