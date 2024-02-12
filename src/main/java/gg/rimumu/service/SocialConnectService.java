package gg.rimumu.service;

import gg.rimumu.dto.Member;

import javax.servlet.http.HttpServletRequest;

public interface SocialConnectService {

    String getLogin();

    String getToken(HttpServletRequest req);

    String getInfo(String accessToken);

    String login(Member member);

}
