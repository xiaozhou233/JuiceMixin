package cn.xiaozhou233.juicemixin;

import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.internal.metadata.MixinDefinition;
import cn.xiaozhou233.juicemixin.internal.metadata.MixinMetadataReader;
import cn.xiaozhou233.juicemixin.internal.transform.TargetTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Public entry point that applies mixins to their target classes.
 * <p>
 * Instances are configured through {@link MixinConfig} and can be reused. The
 * transformer keeps no mutable state between calls, so one instance may transform
 * several independent sets of mixins.
 */
public final class MixinTransformer {

    private final MixinConfig config;

    public MixinTransformer(MixinConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public Map<String, byte[]> transform(Class<?>... mixins) {
        Objects.requireNonNull(mixins, "mixins");
        return transform(Arrays.asList(mixins));
    }

    /**
     * Applies the given mixins and returns all modified target classes keyed by
     * their internal name. Targets are written through the configured
     * {@code OutputProvider} as well.
     * <p>
     * Mixins are grouped by target and applied in registration order. Duplicate
     * mixin classes are ignored.
     *
     * @param mixins mixin classes annotated with {@code @Mixin}
     * @return modified classes keyed by internal name
     * @throws MixinException if a mixin or target cannot be read, parsed or
     *                        transformed
     */
    public Map<String, byte[]> transform(Collection<Class<?>> mixins) {
        Objects.requireNonNull(mixins, "mixins");

        LinkedHashSet<Class<?>> distinctMixins = new LinkedHashSet<>();
        for (Class<?> mixin : mixins) {
            if (mixin == null) {
                throw new MixinException("Mixin list contains a null element");
            }
            distinctMixins.add(mixin);
        }

        List<MixinDefinition> definitions =
                new MixinMetadataReader(config).read(distinctMixins);

        Map<String, List<MixinDefinition>> byTarget = new LinkedHashMap<>();
        for (MixinDefinition definition : definitions) {
            byTarget
                    .computeIfAbsent(
                            definition.targetInternalName(),
                            key -> new ArrayList<>()
                    )
                    .add(definition);
        }

        Map<String, byte[]> transformed = new LinkedHashMap<>();
        TargetTransformer targetTransformer = new TargetTransformer(config);

        for (Map.Entry<String, List<MixinDefinition>> entry : byTarget.entrySet()) {
            transformed.put(
                    entry.getKey(),
                    targetTransformer.transform(entry.getValue())
            );
        }

        return transformed;
    }
}
