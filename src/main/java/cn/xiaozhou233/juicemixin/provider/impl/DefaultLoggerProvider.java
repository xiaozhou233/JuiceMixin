package cn.xiaozhou233.juicemixin.provider.impl;

import cn.xiaozhou233.juicemixin.provider.LoggerProvider;

import java.io.PrintStream;

/**
 * Simple logger writing to {@code System.out} / {@code System.err}.
 * <p>
 * {@code {}} placeholders in the message are replaced by the given arguments,
 * in order. Extra arguments are appended.
 */
public class DefaultLoggerProvider implements LoggerProvider {

    @Override
    public void info(String str, Object... o) {
        log(System.out, "INFO", str, o);
    }

    @Override
    public void warn(String str, Object... o) {
        log(System.out, "WARN", str, o);
    }

    @Override
    public void error(String str, Object... o) {
        log(System.err, "ERROR", str, o);
    }

    @Override
    public void debug(String str, Object... o) {
        log(System.out, "DEBUG", str, o);
    }

    private static void log(PrintStream stream, String level, String message, Object... args) {
        stream.println("[" + level + "] " + format(message, args));
    }

    private static String format(String message, Object[] args) {
        if (message == null) {
            return "null";
        }
        if (args == null || args.length == 0) {
            return message;
        }

        StringBuilder result = new StringBuilder(message.length() + 16);
        int argumentIndex = 0;
        int cursor = 0;
        int placeholder;

        while ((placeholder = message.indexOf("{}", cursor)) >= 0) {
            result.append(message, cursor, placeholder);
            if (argumentIndex < args.length) {
                result.append(args[argumentIndex++]);
            } else {
                result.append("{}");
            }
            cursor = placeholder + 2;
        }
        result.append(message, cursor, message.length());

        while (argumentIndex < args.length) {
            result.append(' ').append(args[argumentIndex++]);
        }

        return result.toString();
    }
}
