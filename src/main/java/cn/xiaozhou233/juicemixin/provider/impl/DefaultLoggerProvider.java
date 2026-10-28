package cn.xiaozhou233.juicemixin.provider.impl;

import cn.xiaozhou233.juicemixin.provider.LoggerProvider;

import java.util.Arrays;

public class DefaultLoggerProvider implements LoggerProvider {

    @Override
    public void info(String str, Object... o) {
        System.out.println("[INFO]" + str + " " + Arrays.toString(o));
    }

    @Override
    public void warn(String str, Object... o) {
        System.out.println("[WARN]" + str + " " + Arrays.toString(o));

    }

    @Override
    public void error(String str, Object... o) {
        System.err.println("[ERROR]" + str + " " + Arrays.toString(o));
    }

    @Override
    public void debug(String str, Object... o) {
        System.out.println("[DEBUG]" + str + " " + Arrays.toString(o));
    }
}
