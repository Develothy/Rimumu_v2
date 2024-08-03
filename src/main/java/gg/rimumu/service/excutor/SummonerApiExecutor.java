package gg.rimumu.service.excutor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gg.rimumu.common.key.RimumuKey;
import gg.rimumu.common.util.HttpConnUtil;
import gg.rimumu.dto.Match;
import gg.rimumu.dto.Summoner;
import gg.rimumu.exception.RimumuException;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class SummonerApiExecutor {

    private static final Gson gson = new Gson();


    public List<?> apiParallelCalls(Summoner item, List<String> endpoints) {

        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (String endpoint : endpoints) {
            CompletableFuture<?> future = CompletableFuture.supplyAsync(() -> callApiForMatch(item, endpoint));
            futures.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }


    private Match callApiForMatch(Summoner summoner, String matchId) {
        try {
            Match match = new Match();
            String matchDataUrl = RimumuKey.SUMMONER_MATCHDTL_URL + matchId.replace("\"", "");
            System.out.println(matchDataUrl);
            HttpResponse<String> matchInfoResponse = HttpConnUtil.sendHttpGetRequest(matchDataUrl);
            JsonObject matchResult = gson.fromJson(matchInfoResponse.body(), JsonObject.class);
            //matchResult 중 info : xx 부분
            match = gson.fromJson(matchResult.getAsJsonObject("info"), Match.class);
            match.setMatchId(matchId);
            match.setSummonerPuuid(summoner.getPuuid());
            match.afterPropertiesSet();
            System.out.println(matchDataUrl.toString());
            System.out.println(match.toString());
            System.out.println("execute "+match.getParticipants().get(0).toString());
            return match;

        } catch (RimumuException e) {
            new RimumuException();
        }
        return null;
    }

}