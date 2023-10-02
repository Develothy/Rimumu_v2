package gg.rimumu.util;

import gg.rimumu.common.RimumuKey;
import gg.rimumu.exception.RimumuException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpConnUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnUtil.class);
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static HttpResponse sendHttpGetRequest(String url) throws RimumuException {
        return sendHttpGetRequest(url, true);
    }

    public static HttpResponse sendHttpGetRequest(String url, boolean retry) throws RimumuException {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .setHeader("X-Riot-Token", RimumuKey.API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response;
            if (retry) {
                response = sendRequestWithRetry(req, url);
            } else {
                response = sendRequest(req);
            }

            return response;
        } catch (Exception e) {
            throw new RimumuException(500, e.getMessage());
        }
    }

    private static HttpResponse<String> sendRequestWithRetry(HttpRequest request, String url) throws RimumuException {

        int retry = 0;
        while (retry < 5) {
            HttpResponse<String> response = sendRequest(request);
            if (response.statusCode() == 200) {
                return response;
            }
            retry++;
            LOGGER.info("{} : retry...{}", url, retry);

            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                LOGGER.error("Retry waiting interrupted");
                throw new RimumuException(url + " retry waiting interrupted");
            }
        }

        throw new RimumuException(url + " exceeded maximum retry attempts");
    }

    private static HttpResponse<String> sendRequest(HttpRequest request) throws RimumuException {
        try {
            return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RimumuException(500, e.getMessage());
        }
    }
}
