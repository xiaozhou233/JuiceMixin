package cn.xiaozhou233.juicemixin;

import cn.xiaozhou233.juicemixin.provider.BytecodeProvider;
import cn.xiaozhou233.juicemixin.provider.ClassProvider;
import cn.xiaozhou233.juicemixin.provider.LoggerProvider;
import cn.xiaozhou233.juicemixin.provider.impl.DefaultBytecodeProvider;
import cn.xiaozhou233.juicemixin.provider.impl.DefaultClassProvider;
import cn.xiaozhou233.juicemixin.provider.impl.DefaultLogger;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class JuiceMixin {
    @Getter
    private static ClassProvider classProvider;
    @Getter
    private static BytecodeProvider bytecodeProvider;
    @Getter
    private static LoggerProvider loggerProvider;

    public static void init(@Nullable ClassProvider classProvider, @Nullable BytecodeProvider bytecodeProvider, @Nullable LoggerProvider loggerProvider) {
        JuiceMixin.classProvider = classProvider != null
                ? classProvider : new DefaultClassProvider();

        JuiceMixin.bytecodeProvider = bytecodeProvider != null
                ? bytecodeProvider : new DefaultBytecodeProvider();

        JuiceMixin.loggerProvider = loggerProvider != null
                ? loggerProvider : new DefaultLogger();
    }

    public static void init() { init(null,null,null);}
    public static void init(ClassProvider classProvider, BytecodeProvider bytecodeProvider) { init(classProvider, bytecodeProvider, null);}
}
