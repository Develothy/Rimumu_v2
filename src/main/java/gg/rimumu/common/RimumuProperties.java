package gg.rimumu.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rimumu")
@Getter @Setter
public class RimumuProperties {

/*
    @Autowired
    private Environment environment ;
*/

    public static String api_key ;
    public String encrypt_key ;
    public static String dd_version_url;
    public static String dd_url;
    public static String summoner_info_url ;
    public static String summoner_tier_url;
    public static String summoner_current_url ;
    public static String summoner_matches_url ;
    public static String summoner_matchdtl_url ;

/*    @PostConstruct
    private void initialize() {

        api_key = environment.getProperty("api_key");
        encrypt_key = environment.getProperty("encrypt_key");
        dd_version_url = environment.getProperty("dd_version_url");
        dd_url = environment.getProperty("dd_url");
        summoner_info_url = environment.getProperty("summoner_info_url");
        summoner_tier_url = environment.getProperty("summoner_tier_url");
        summoner_current_url = environment.getProperty("summoner_current_url");
        summoner_matches_url = environment.getProperty("summoner_matches_url");
        summoner_matchdtl_url = environment.getProperty("summoner_matchdtl_url");
    }*/
}
