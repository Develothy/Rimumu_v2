package gg.rimumu.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gg.rimumu.common.util.HttpConnUtil;
import gg.rimumu.dto.Member;
import gg.rimumu.dto.MemberToken;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class KakaoConnectService implements SocialConnectService {

    @Value("${oauth.kakao.client_id}")
    private String KAKAO_CLIENT_ID;

    @Value("${oauth.kakao.cclient_secret}")
    private String KAKAO_CLIENT_SECRET;

    //@Value("${oauth.kakao.redirect_url}")
    private String KAKAO_REDIRECT_URL = "http://localhost:8088/login/callback";

    private final String KAKAO_AUTH_URI = "https://kauth.kakao.com";
    private final String KAKAO_API_URI = "https://kapi.kakao.com";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();


    @Override
    public String getLogin() {
        System.out.println("token 진입");
        return KAKAO_AUTH_URI + "/oauth/authorize"
                + "?client_id=" + KAKAO_CLIENT_ID
                + "&redirect_uri=" + KAKAO_REDIRECT_URL
                + "&response_type=code"
                + "&state=kakao";
    }

    @Override
    public String getToken(HttpServletRequest req) {
        System.out.println("info 진입");

        MemberToken memberToken = new MemberToken();
        String uri = KAKAO_AUTH_URI + "/oauth/token";
        String param = "grant_type=authoriaztion_code"
                + "&client_id=" + KAKAO_CLIENT_ID
                + "&client_secret=" + KAKAO_CLIENT_SECRET
                + "&code=" + req.getParameter("code")
                + "&redirect_uri=" + KAKAO_REDIRECT_URL;

        try {
            HttpRequest req2 = HttpRequest.newBuilder()
                    .uri(new URI(uri))
                    .setHeader("Content-type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(param))
                    .build();


            HttpResponse<String> response = CLIENT.send(req2, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "카카오 통신 에러";
            }

            JsonObject data = JsonParser.parseString(response.body()).getAsJsonObject();
            memberToken.setAccessToken(data.get("access_token").getAsString());
            memberToken.setRefreshToken(data.get("refresh_token").getAsString());
            return memberToken.getAccessToken();

        } catch (Exception e) {

        }


        return KAKAO_AUTH_URI ;
    }

    @Override
    public String getInfo(String accessToken) {

        try {

            String url = KAKAO_API_URI + "/v2/user/me";
            HttpRequest req = HttpRequest.newBuilder()
                    .setHeader("Authorization", "Bearer " + accessToken)
                    .setHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                    .GET()
                    .build();

            HttpResponse<String> response;

            //HttpHeader 담기
            response = HttpConnUtil.sendRequestWithRetry(req, url);

            //Response 데이터 파싱

            JsonObject result = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject account = JsonParser.parseString(result.get("kakao_account").getAsString()).getAsJsonObject();
            JsonObject profile = JsonParser.parseString(account.get("profile").getAsString()).getAsJsonObject();

            Long id = result.get("id").getAsLong();
            String email = String.valueOf(account.get("email"));
            String nickname = String.valueOf(profile.get("nickname"));
        } catch (Exception e) {

        }

        return null;
    }

    @Override
    public String login(Member member) {
        return null;
    }
}
