package gg.rimumu.service.excutor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gg.rimumu.common.key.RimumuKey;
import gg.rimumu.common.util.HttpConnUtil;
import gg.rimumu.dto.Match;
import gg.rimumu.dto.Summoner;
import gg.rimumu.exception.RimumuException;
import gg.rimumu.service.SummonerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class SummonerApiExecutor {

    private static final Gson gson = new Gson();


    public List<JsonObject> apiParallelCalls(Summoner item, List<String> endpoints) {

        List<CompletableFuture<JsonObject>> futures = new ArrayList<>();

        for (String endpoint : endpoints) {
            CompletableFuture<JsonObject> future = CompletableFuture.supplyAsync(() -> callApiForMatch(item, endpoint));
            futures.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }


    private JsonObject callApiForMatch(Summoner summoner, String matchId) {
        try {
            String matchDataUrl = RimumuKey.SUMMONER_MATCHDTL_URL + matchId.replace("\"", "");


            HttpResponse<String> matchInfoResponse = HttpConnUtil.sendHttpGetRequest(matchDataUrl);
            JsonObject matchResult = gson.fromJson(matchInfoResponse.body(), JsonObject.class);
            //matchResult 중 info : xx 부분
            JsonObject info = matchResult.getAsJsonObject("info");
            return info;

        } catch (RimumuException e) {
            new RimumuException();
        }
        return null;
    }

}