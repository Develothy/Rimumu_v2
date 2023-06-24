package gg.rimumu.util;

import gg.rimumu.common.RimumuKey;
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

    public static String DD_VERSION = "13.12.2";

    public static HttpClient client = HttpClient.newHttpClient();

    public HttpConnUtil() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    static {
        try {
            String json = sendHttpGetRequest(RimumuKey.DD_VERSION_URL);
            int end = json.indexOf("\"", 2);
            DD_VERSION = json.substring(2, end);
            System.out.println(DD_VERSION);
        } catch (Exception e) {
            System.out.println("version init fail");
        }
    }

    public static String sendHttpGetRequest(String url) {

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .setHeader("X-Riot-Token", RimumuKey.API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.out.println("Exception 발생! " + e.getMessage());
        }
        return null;
    }

}
