package gg.rimumu.common.key;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RimumuKey {

    @Autowired
    private Environment environment ;

    public static String API_KEY ;
    public static String ENCRYPT_KEY ;
    public static String DD_VERSION_URL;
    public static String DD_URL;
    public static String SUMMONER_INFO_URL ;
    public static String SUMMONER_TIER_URL;
    public static String SUMMONER_MASTERY_URL;
    public static String SUMMONER_CURRENT_URL ;
    public static String SUMMONER_MATCHES_URL ;
    public static String SUMMONER_MATCHDTL_URL ;

    @PostConstruct
    private void initialize() {

        API_KEY = environment.getProperty("API_KEY");
        ENCRYPT_KEY = environment.getProperty("ENCRYPT_KEY");
        DD_VERSION_URL = environment.getProperty("DD_VERSION_URL");
        DD_URL = environment.getProperty("DD_URL");
        SUMMONER_INFO_URL = environment.getProperty("SUMMONER_INFO_URL");
        SUMMONER_TIER_URL = environment.getProperty("SUMMONER_TIER_URL");
        SUMMONER_MASTERY_URL = environment.getProperty("SUMMONER_MASTERY_URL");
        SUMMONER_CURRENT_URL = environment.getProperty("SUMMONER_CURRENT_URL");
        SUMMONER_MATCHES_URL = environment.getProperty("SUMMONER_MATCHES_URL");
        SUMMONER_MATCHDTL_URL = environment.getProperty("SUMMONER_MATCHDTL_URL");
    }
}
