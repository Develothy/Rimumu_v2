package gg.rimumu.common.audit.aspect.audit;

import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    String target() default "";

    Class<?> targetClass() default Object.class;

    AuditAction action() default AuditAction.NONE;

}
