package gg.rimumu.service;

import gg.rimumu.dto.Member;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;

@Service
public class MemberService {

    @Autowired
    private BeanFactory beanFactory;
    private SocialConnectService service;

    public String login(Member member) {

        if (ObjectUtils.isEmpty(member.getEmail())) {
            // 일반 로그인
            return "";
        }

        return findConnect(member.getSocial()).getLogin();
    }

    public String getSocialInfo(HttpServletRequest req) {


        Member.Social social = Member.Social.valueOf(req.getParameter("state"));
        String accessToken = findConnect(social).getToken(req);

        return findConnect(social).getInfo(accessToken);
    }

    private SocialConnectService findConnect(Member.Social social) {
        switch (social.label()) {
            case "kakao" : return beanFactory.getBean(KakaoConnectService.class);
            default : return beanFactory.getBean(SocialConnectService.class);
        }
    }
}
