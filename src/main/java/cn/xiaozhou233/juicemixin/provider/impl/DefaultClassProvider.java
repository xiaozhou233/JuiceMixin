package cn.xiaozhou233.juicemixin.provider.impl;

import cn.xiaozhou233.juicemixin.JuiceMixin;
import cn.xiaozhou233.juicemixin.provider.ClassProvider;

public class DefaultClassProvider implements ClassProvider {
    @Override
    public Class<?> get(String name) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = DefaultClassProvider.class.getClassLoader();
        }

        try {
            return Class.forName(name, false, classLoader);
        } catch (ClassNotFoundException e) {
            JuiceMixin.getLoggerProvider().error(
                    "Cannot find class {}: {}", name, e.getMessage()
            );
            return null;
        }
    }
}
