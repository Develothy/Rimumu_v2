package gg.rimumu.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


@Component
public class HttpConnUtil {

    @Value("${LoL.KEY}")
    private static String API_KEY;

    private static String VERSION;

    private static HttpClient client = HttpClient.newHttpClient();

    public HttpConnUtil() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    static {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI("https://ddragon.leagueoflegends.com/api/versions.json"))
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            String json = res.body();
            int end = json.indexOf("\"", 2);
            VERSION = json.substring(2, end);
            System.out.println(VERSION);

        } catch (Exception e) {
            System.out.println("version init fail");
        }
    }

    public static String sendHttpGetRequest(String url) {

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .setHeader("X-Riot-Token", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {

        }
        return null;
    }

}
