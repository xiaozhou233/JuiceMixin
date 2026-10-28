package cn.xiaozhou233.juicemixin.provider.impl;

import cn.xiaozhou233.juicemixin.JuiceMixin;
import cn.xiaozhou233.juicemixin.provider.BytecodeProvider;
import cn.xiaozhou233.juicemixin.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class DefaultBytecodeProvider implements BytecodeProvider {

    @Override
    public byte[] get(Class<?> clazz) {
        String resourcePath = clazz.getName().replace('.', '/') + ".class";
        ClassLoader classLoader = clazz.getClassLoader();

        try (InputStream inputStream = classLoader != null
                ? classLoader.getResourceAsStream(resourcePath)
                : ClassLoader.getSystemResourceAsStream(resourcePath)) {

            if (inputStream == null) {
                JuiceMixin.getLoggerProvider().error("Cannot find bytecode for class: " + clazz.getName());
            }

            return IOUtils.readStream(inputStream);
        } catch (IOException e) {
            JuiceMixin.getLoggerProvider().error("Failed to read bytecode for class: " + clazz.getName());
            return null;
        }
    }
}