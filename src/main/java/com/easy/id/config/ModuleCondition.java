package com.easy.id.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

public class ModuleCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(Module.class.getName());
        if (attributes == null) {
            return true;
        }
        final String prefix = Optional.ofNullable(attributes.getFirst("prefix")).map(Object::toString).orElse("");
        final Environment environment = context.getEnvironment();
        for (Object value : attributes.get("value")) {
            String[] moduleName = (String[]) value;
            for (String module : moduleName) {
                if (environment.getProperty(prefix + "." + module, boolean.class, false)) {
                    return true;
                }
            }
        }
        return false;
    }
}
