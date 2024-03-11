package gg.rimumu.base.resolver;

import gg.rimumu.common.audit.common.Actor;
import gg.rimumu.common.audit.common.ClientIp;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

public class ActorResolver implements HandlerMethodArgumentResolver  {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Actor.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        try {
            return Actor.builder()
                    .username(Objects.requireNonNull(webRequest.getHeader("rim-username")))
                    .clientIp(ClientIp.builder().ip(getClientIp(webRequest)).build())
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("user-info is missing.");
        }
    }

    private String getClientIp(NativeWebRequest request) {
        return ((HttpServletRequest) request.getNativeRequest()).getRemoteAddr();
    }

}
