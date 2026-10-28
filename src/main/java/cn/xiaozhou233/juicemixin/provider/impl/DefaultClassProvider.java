package cn.xiaozhou233.juicemixin.provider.impl;

import cn.xiaozhou233.juicemixin.JuiceMixin;
import cn.xiaozhou233.juicemixin.provider.ClassProvider;

public class DefaultClassProvider implements ClassProvider {
    @Override
    public Class<?> get(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            JuiceMixin.getLoggerProvider().error("Exception: ", e);
            return null;
        }
    }
}
