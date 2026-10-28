package cn.xiaozhou233.juicemixin;

import cn.xiaozhou233.juicemixin.provider.BytecodeProvider;
import cn.xiaozhou233.juicemixin.provider.ClassProvider;
import cn.xiaozhou233.juicemixin.provider.LoggerProvider;
import cn.xiaozhou233.juicemixin.provider.OutputProvider;
import cn.xiaozhou233.juicemixin.provider.impl.DefaultBytecodeProvider;
import cn.xiaozhou233.juicemixin.provider.impl.DefaultClassProvider;
import cn.xiaozhou233.juicemixin.provider.impl.DefaultLoggerProvider;
import cn.xiaozhou233.juicemixin.provider.impl.EmptyOutputProvider;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class JuiceMixin {
    @Getter
    private static ClassProvider classProvider = new DefaultClassProvider();
    @Getter
    private static BytecodeProvider bytecodeProvider = new DefaultBytecodeProvider();
    @Getter
    private static LoggerProvider loggerProvider = new DefaultLoggerProvider();
    @Setter
    @Getter
    private static OutputProvider outputProvider = new EmptyOutputProvider();

    public static void init(@Nullable ClassProvider classProvider, @Nullable BytecodeProvider bytecodeProvider, @Nullable LoggerProvider loggerProvider) {
        JuiceMixin.classProvider = classProvider != null
                ? classProvider : new DefaultClassProvider();

        JuiceMixin.bytecodeProvider = bytecodeProvider != null
                ? bytecodeProvider : new DefaultBytecodeProvider();

        JuiceMixin.loggerProvider = loggerProvider != null
                ? loggerProvider : new DefaultLoggerProvider();
    }

    public static void init() { init(null,null,null);}
    public static void init(ClassProvider classProvider, BytecodeProvider bytecodeProvider) { init(classProvider, bytecodeProvider, null);}
}
