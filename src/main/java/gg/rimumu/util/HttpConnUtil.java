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

    public static String DD_VERSION = "13.12.1";

    public static HttpClient client = HttpClient.newHttpClient();

    public HttpConnUtil() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    static {
        try {
            HttpResponse response = sendHttpGetRequest(RimumuKey.DD_VERSION_URL);
            if (response.statusCode() == 200) {
                String json = (String) response.body();
                int end = json.indexOf("\"", 2);
                String DD_VERSION = json.substring(2, end);
                System.out.println(DD_VERSION);
            }
        } catch (Exception e) {
            DD_VERSION = "13.12.1";
            System.out.println("version sendHttpGetRequest fail. version : " + DD_VERSION);
        }

    }

    public static HttpResponse sendHttpGetRequest(String url) {

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .setHeader("X-Riot-Token", RimumuKey.API_KEY)
                    .GET()
                    .build();

            return client.send(req, HttpResponse.BodyHandlers.ofString());

        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.out.println("Exception 발생! " + e.getMessage());
        }
        return null;
    }

}
