package gg.rimumu.util;

import gg.rimumu.common.RimumuKey;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;

@Component
public class VersionSet implements ApplicationRunner {

    public static String DD_VERSION = "13.12.0";

    @Override
    public void run(ApplicationArguments args) {
        versionInit();
    }

    public String versionInit() {
        try {
            HttpResponse response = HttpConnUtil.sendHttpGetRequest(RimumuKey.DD_VERSION_URL);
            if (response.statusCode() == 200) {
                String json = (String) response.body();
                int end = json.indexOf("\"", 2);
                DD_VERSION = json.substring(2, end);
                System.out.println("Generate version : " + DD_VERSION);
                return "Generate version : " + DD_VERSION;
            }
        } catch (Exception e) {
            System.out.println("version sendHttpGetRequest fail. version : " + DD_VERSION);
        }
        return null;
    }

    public String versionSet(String version) {
        DD_VERSION = version;
        return DD_VERSION;
    }
}
