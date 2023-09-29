package gg.rimumu.service.excutor;

import gg.rimumu.dto.Match;
import gg.rimumu.dto.Summoner;
import gg.rimumu.exception.RimumuException;
import gg.rimumu.service.SummonerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SummonerApiExecutor {

    @Autowired
    private final SummonerService summonerService;


    public List<Match> apiParallelCalls(Summoner item, List<String> endpoints) throws RimumuException, ExecutionException, InterruptedException {

        List<CompletableFuture<Match>> futures = new ArrayList<>();

        for (String endpoint : endpoints) {
            CompletableFuture<Match> future = CompletableFuture.supplyAsync(() -> callApiForMatch(item, endpoint));
            futures.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }


    private Match callApiForMatch(Summoner summoner, String endpoint) {
        Match match = new Match();
        try {
            match = summonerService.setMatchDtls(summoner, endpoint);
        } catch (RimumuException e) {
            new RimumuException();
        }
        return match;
    }

}