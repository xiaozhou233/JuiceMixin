package cn.xiaozhou233.juicemixin;

import cn.xiaozhou233.juicemixin.provider.BytecodeProvider;
import cn.xiaozhou233.juicemixin.provider.ClassProvider;
import cn.xiaozhou233.juicemixin.provider.LoggerProvider;
import cn.xiaozhou233.juicemixin.provider.OutputProvider;
import cn.xiaozhou233.juicemixin.provider.impl.DefaultBytecodeProvider;
import cn.xiaozhou233.juicemixin.provider.impl.DefaultClassProvider;
import cn.xiaozhou233.juicemixin.provider.impl.DefaultLoggerProvider;
import cn.xiaozhou233.juicemixin.provider.impl.EmptyOutputProvider;

import java.util.Objects;

/**
 * Immutable configuration of a {@link MixinTransformer}.
 * <p>
 * All extension points are injected here instead of being stored in static
 * fields, so several transformers with different providers can coexist in the
 * same JVM.
 */
public final class MixinConfig {

    private final BytecodeProvider bytecodeProvider;
    private final ClassProvider classProvider;
    private final LoggerProvider loggerProvider;
    private final OutputProvider outputProvider;

    private MixinConfig(Builder builder) {
        this.bytecodeProvider = Objects.requireNonNull(
                builder.bytecodeProvider, "bytecodeProvider"
        );
        this.classProvider = Objects.requireNonNull(builder.classProvider, "classProvider");
        this.loggerProvider = Objects.requireNonNull(builder.loggerProvider, "loggerProvider");
        this.outputProvider = Objects.requireNonNull(builder.outputProvider, "outputProvider");
    }

    /**
     * Returns a configuration using the default providers and a no-op output
     * provider.
     *
     * @return the default configuration
     */
    public static MixinConfig defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public BytecodeProvider bytecodeProvider() {
        return bytecodeProvider;
    }

    public ClassProvider classProvider() {
        return classProvider;
    }

    public LoggerProvider loggerProvider() {
        return loggerProvider;
    }

    public OutputProvider outputProvider() {
        return outputProvider;
    }

    public static final class Builder {
        private BytecodeProvider bytecodeProvider = new DefaultBytecodeProvider();
        private ClassProvider classProvider = new DefaultClassProvider();
        private LoggerProvider loggerProvider = new DefaultLoggerProvider();
        private OutputProvider outputProvider = new EmptyOutputProvider();

        private Builder() {
        }

        public Builder bytecodeProvider(BytecodeProvider bytecodeProvider) {
            this.bytecodeProvider = Objects.requireNonNull(
                    bytecodeProvider, "bytecodeProvider"
            );
            return this;
        }

        public Builder classProvider(ClassProvider classProvider) {
            this.classProvider = Objects.requireNonNull(classProvider, "classProvider");
            return this;
        }

        public Builder loggerProvider(LoggerProvider loggerProvider) {
            this.loggerProvider = Objects.requireNonNull(loggerProvider, "loggerProvider");
            return this;
        }

        public Builder outputProvider(OutputProvider outputProvider) {
            this.outputProvider = Objects.requireNonNull(outputProvider, "outputProvider");
            return this;
        }

        public MixinConfig build() {
            return new MixinConfig(this);
        }
    }
}
