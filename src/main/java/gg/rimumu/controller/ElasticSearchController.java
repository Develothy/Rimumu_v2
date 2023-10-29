package gg.rimumu.controller;

import gg.rimumu.common.result.RimumuResult;
import gg.rimumu.service.ElasticSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/es")
public class ElasticSearchController {

    @Autowired
    private final ElasticSearchService elasticSearchService;

    @PostMapping("/init")
    public RimumuResult createIndexAndInsertDoc() {
        return new RimumuResult<>(elasticSearchService.createIndexAndInsertDoc());
    }

    @PostMapping("/index")
    public RimumuResult createIndex() {
        return new RimumuResult<>(elasticSearchService.createIndex());
    }

    @PostMapping("/doc")
    public RimumuResult insertDocument() {
        return new RimumuResult<>(elasticSearchService.indexData());
    }

}
