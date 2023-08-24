package gg.rimumu.util;

import gg.rimumu.common.RimumuKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;

@Component
public class VersionUtil implements ApplicationRunner {

    public static String DD_VERSION = "13.12.0";
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionUtil.class);


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
                LOGGER.info("Generate version : " + DD_VERSION);
                return "Generate version : " + DD_VERSION;
            }
        } catch (Exception e) {
            LOGGER.error("version sendHttpGetRequest fail. version : " + DD_VERSION);
        }
        return null;
    }

    public String versionSet(String version) {
        DD_VERSION = version;
        return DD_VERSION;
    }
}
