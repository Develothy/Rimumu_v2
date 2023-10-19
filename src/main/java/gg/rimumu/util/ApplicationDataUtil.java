package gg.rimumu.util;

import gg.rimumu.common.RimumuKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;

@Component
public class ApplicationDataUtil implements ApplicationRunner {

    public static String DD_VERSION = "13.20.1";
    public static String FILE_PATH = "src/main/resources/datadragon/item.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDataUtil.class);


    @Override
    public void run(ApplicationArguments args) {
        InitVersion();
        InitItemData();
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 0시 0분 0초
    public String InitVersion() {
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

    public void InitItemData() {
        LOGGER.info("Generate item.json version : {}", DD_VERSION);
        try {
            FileUtil.initJsonFile(FILE_PATH,
                    RimumuKey.DD_URL + DD_VERSION + "/data/ko_KR/item.json");
        } catch (Exception e) {
            LOGGER.error("item.json init fail. check!! item.json!!");
        }
    }

    public String serVersion(String version) {
        DD_VERSION = version;
        return DD_VERSION;
    }
}
