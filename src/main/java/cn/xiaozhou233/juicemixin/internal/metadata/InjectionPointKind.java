package cn.xiaozhou233.juicemixin.internal.metadata;

import cn.xiaozhou233.juicemixin.exception.MixinException;

import java.util.Locale;

/**
 * Parsed {@code @At.value()} of an injection point.
 */
public enum InjectionPointKind {
    HEAD,
    TAIL,
    INVOKE,
    FIELD,
    CONSTANT;

    public boolean isMemberBased() {
        return this == INVOKE || this == FIELD || this == CONSTANT;
    }

    public static InjectionPointKind parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new MixinException("Injection point must not be empty");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);

        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new MixinException(
                    "Unknown injection point '" + value
                            + "'; expected HEAD, TAIL, INVOKE, FIELD or CONSTANT",
                    e
            );
        }
    }
}
