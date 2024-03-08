package gg.rimumu.common.audit.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.internal.Primitives;
import gg.rimumu.common.audit.common.Actor;
import gg.rimumu.common.audit.common.ClientIp;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class JoinPointUtil {

    private static Logger log = LoggerFactory.getLogger(JoinPointUtil.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper OBJECT_MAPPER_NON_NULL = new ObjectMapper();

    static {
        OBJECT_MAPPER_NON_NULL.setSerializationInclusion(NON_NULL);
    }

    public static String extractParameters(JoinPoint joinPoint, Class<? extends Annotation> excludeAnnotationType) {
        StringBuilder params = new StringBuilder();
        Object[] args = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = methodSignature.getMethod().getParameters();

        if (ObjectUtils.isEmpty(args)) {
            return params.toString();
        }

        int idx = 0;
        for (Object arg : args) {
            if (parameters[idx].getAnnotation(excludeAnnotationType) != null) {
                idx++;
                continue;
            }

            if (arg == null || args.getClass() == null) {
                params.append("null, ");
                idx++;
                continue;
            }

            // 파라미터 제외 타입 지정 (ip는 로그 공통 파라미터라서 칼럼 따로 뺄거임)
            if (arg instanceof ClientIp || arg instanceof Actor) {
                idx++;
                continue;
            }

            Class<?> clazz = arg.getClass();

            if (clazz.isPrimitive()
                    || Primitives.isWrapperType(clazz)
                    || clazz == String.class
                    || clazz == ArrayList.class) {

                params.append(arg);
            } else {
                if (!clazz.isArray()) {
                    writeValueAsString(arg, params, excludeAnnotationType);
                } else {
                    Object[] argArr = (Object[]) arg;
                    params.append("[");
                    for (int i = 0; i < argArr.length; i++) {
                        writeValueAsString(argArr[i], params, excludeAnnotationType);
                    }
                    params.append("]");
                }
            }

            idx++;
            if (idx < args.length) params.append(", ");
        }

        return params.toString();
    }

    private static void writeValueAsString(Object arg, StringBuilder params, Class<? extends Annotation> excludeAnnotationType) {
        try {
            Class<?> clazz = arg.getClass();
            if (clazz == LinkedHashMap.class) {
                params.append(OBJECT_MAPPER.writeValueAsString(arg));
                return;
            }

            Object clone = clazz.newInstance();
            List<String> excludeFields = getExcludeFields(clazz, excludeAnnotationType);
            if (ObjectUtils.isNotEmpty(excludeFields)) {
                BeanUtils.copyProperties(arg, clone, excludeFields.toArray(new String[0]));
                params.append(OBJECT_MAPPER_NON_NULL.writeValueAsString(clone));
            } else {
                BeanUtils.copyProperties(arg, clone);
                params.append(OBJECT_MAPPER.writeValueAsString(clone));
            }

        } catch (Exception e) {
            log.warn("Exception : {}", arg.toString());
            params.append(arg.toString());
        }
    }

    private static List<String> getExcludeFields(Class<?> clazz, Class<? extends Annotation> excludeAnnotationType) {
        List<String> excludeFields = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Annotation excludeAnnotation = field.getAnnotation(excludeAnnotationType);
            if (ObjectUtils.isNotEmpty(excludeAnnotation)) {
                excludeFields.add(field.getName());
            }
        }
        return excludeFields;
    }

    public static Long extractAnnotatedLongValue(JoinPoint joinPoint, Class<? extends Annotation> annotationType) {
        Object[] args = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = methodSignature.getMethod().getParameters();

        if (args.length == 0
                || parameters.length == 0
                || args.length != parameters.length) {

            return null;
        }

        return doeExtractAnnotatedValue(args, parameters, annotationType);
    }

    private static Long doeExtractAnnotatedValue(Object[] args, Parameter[] parameters, Class<? extends Annotation> annotationType) {
        for (int index = 0; index < args.length; index++) {
            if (isNull(args[index]) || isNull(args[index].getClass())) {
                continue;
            }

            Object arg = args[index];
            Parameter parameter = parameters[index];

            boolean isParamAnnotated = nonNull(parameter.getAnnotation(annotationType));
            if (isParamAnnotated) {
                Class<?> type = parameter.getType();

                if (isLongType(type)) {
                    return (Long) arg;
                }
                if (isIntType(type)) {
                    return ((Integer) arg).longValue();
                }
            }

        }
        return null;
    }

    private static boolean isLongType(Class<?> type) {
        return Long.TYPE == type || long.class == type;
    }

    private static boolean isIntType(Class<?> type) {
        return Integer.TYPE == type || int.class == type;
    }

    public static Actor extractActor(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (isNull(args)) {
            log.warn("joinpoint args is null");
            return null;
        }
        log.info("args length : ", args.length);

        Actor actor = doExtractActor(args);

        if (ObjectUtils.isEmpty(actor.getUsername())) {
            log.warn("joinpoint actor getUsername is null");
            return null;
        }

        if (ObjectUtils.isEmpty(actor.getClientIp())) {
            log.warn("joinpoint actor getClientIp is null");
            return null;
        }

        return actor;
    }

    private static Actor doExtractActor(Object[] args) {
        Actor.ActorBuilder builder = Actor.builder();
        for (Object arg: args) {
            if (arg instanceof Actor) {
                Actor actor = (Actor) arg;
                String actorStr = actor.toString();
                log.info("actor : {}", actorStr);
                if (ObjectUtils.isNotEmpty(actor.getUsername())) {
                    builder.username(actor.getUsername());
                }

                if (ObjectUtils.isNotEmpty(actor.getClientIp())) {
                    builder.clientIp(actor.getClientIp());
                }

            } else if (arg instanceof ClientIp) {
                ClientIp clientIp = (ClientIp) arg;
                builder.clientIp(clientIp);
            }
        }
        return builder.build();
    }

    public static String extractSignature(JoinPoint joinPoint, Class<? extends Annotation> excludeAnnotationType) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = methodSignature.getMethod().getParameters();

        String parameterStr = Arrays.stream(parameters)
                .filter(param -> ObjectUtils.isEmpty(param.getAnnotation(excludeAnnotationType)))
                .map(Parameter::getName)
                .collect(Collectors.joining(", "));

        return methodSignature.toShortString().replace("..", parameterStr);
    }

}
