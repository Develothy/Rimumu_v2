package gg.rimumu.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SummonerService {

    @Value("${LoL_KEY}")
    private String API_KEY;

    @Value("${DDUrl}")
    private String ddUrl;

    @Value("${DDVer}")
    private String ddVer;





}
