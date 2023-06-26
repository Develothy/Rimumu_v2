package gg.rimumu.controller;

import gg.rimumu.common.ChampionKey;
import gg.rimumu.util.VersionSet;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/lol")
@RestController
@RequiredArgsConstructor
public class LoLController {

    private final VersionSet versionSet;

    @GetMapping("/version")
    public String version() {
        return VersionSet.DD_VERSION;
    }

    @PutMapping("/version")
    public String init() {
        return versionSet.versionInit();
    }
    @PostMapping("/version")
    public String set(@RequestParam String version) {
        return versionSet.versionSet(version);
    }

    @GetMapping("/champion")
    public Map list() {
        return ChampionKey.valuesWithLabel();
    }

    @PostMapping("/champion")
    public String add(@RequestParam String key, @RequestParam String champion) {
        return ChampionKey.addChampion(key, champion);
    }
}
