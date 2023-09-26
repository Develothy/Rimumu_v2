package gg.rimumu.service;

import gg.rimumu.dto.Match;
import gg.rimumu.dto.Summoner;
import gg.rimumu.exception.RimumuException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    @Autowired
    private final SummonerService summonerService;


    public Object ApiParallelCalls(ReturnType type, Object item, List<String> endpoints) throws RimumuException, ExecutionException, InterruptedException {

        List<Object> results = new ArrayList<>();
        List<Supplier<Object>> tasks = new ArrayList<>();

        for (String endpoint : endpoints) {
            /**
             * ApiParallelCalls을 사용하는 ReturnType 추가될 경우 (ex.ITEM)
             * ReturnType enum 추가
             */
            switch (type) {
                case MATCH -> tasks.add(() -> callApiForMatch((Summoner) item, endpoint));
                default -> tasks.add(() -> callApiForMatch((Summoner) item, endpoint));
            }
        }

        List<CompletableFuture<Object>> futures = tasks.stream()
                .map(task -> CompletableFuture.supplyAsync(task))
                .collect(Collectors.toList());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        for (CompletableFuture future : futures) {
            results.add(future.get());
        }

        return results;
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

    public enum ReturnType {
        MATCH;
    }

}