package cn.xiaozhou233.juicemixin;

import java.util.Map;

/**
 * Static convenience facade for the default transformer.
 * <p>
 * The library itself does not keep any global mutable state. Prefer constructing
 * a {@link MixinTransformer} with an explicit {@link MixinConfig} when custom
 * providers are needed, or when several isolated transformers are used in the
 * same JVM.
 */
public final class JuiceMixin {

    private JuiceMixin() {
    }

    /**
     * Creates a transformer with the default configuration.
     *
     * @return a new transformer
     */
    public static MixinTransformer transformer() {
        return new MixinTransformer(MixinConfig.defaults());
    }

    /**
     * Creates a transformer with an explicit configuration.
     *
     * @param config transformer configuration
     * @return a new transformer
     */
    public static MixinTransformer transformer(MixinConfig config) {
        return new MixinTransformer(config);
    }

    /**
     * Convenience method that transforms mixins with the default configuration.
     *
     * @param mixins mixin classes annotated with {@code @Mixin}
     * @return modified classes keyed by internal name
     */
    public static Map<String, byte[]> transform(Class<?>... mixins) {
        return transformer().transform(mixins);
    }
}
