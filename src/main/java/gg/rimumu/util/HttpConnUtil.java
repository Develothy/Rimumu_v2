package gg.rimumu.util;

import gg.rimumu.common.RimumuProperties;
import gg.rimumu.exception.RimumuException;
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

    public static HttpClient client = HttpClient.newHttpClient();

    public HttpConnUtil() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public static HttpResponse sendHttpGetRequest(String url) {

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .setHeader("X-Riot-Token", RimumuProperties.api_key)
                    .GET()
                    .build();

            return client.send(req, HttpResponse.BodyHandlers.ofString());

        } catch (URISyntaxException | IOException | InterruptedException e) {
            new RimumuException (e.getMessage());
        }
        return null;
    }

}
