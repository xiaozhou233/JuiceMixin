package cn.xiaozhou233.juicemixin.internal.injection;

import cn.xiaozhou233.juicemixin.internal.metadata.InjectionPointKind;

import java.util.EnumMap;
import java.util.Map;

/**
 * Provides the strategy for a parsed injection point kind.
 */
public final class InjectionPointFactory {

    private static final Map<InjectionPointKind, InjectionPoint> POINTS =
            new EnumMap<>(InjectionPointKind.class);

    static {
        POINTS.put(InjectionPointKind.HEAD, new HeadInjectionPoint());
        POINTS.put(InjectionPointKind.TAIL, new TailInjectionPoint());
        POINTS.put(InjectionPointKind.INVOKE, new InvokeInjectionPoint());
        POINTS.put(InjectionPointKind.FIELD, new FieldInjectionPoint());
        POINTS.put(InjectionPointKind.CONSTANT, new ConstantInjectionPoint());
    }

    private InjectionPointFactory() {
    }

    public static InjectionPoint get(InjectionPointKind kind) {
        InjectionPoint point = POINTS.get(kind);
        if (point == null) {
            throw new IllegalArgumentException("No injection point for " + kind);
        }
        return point;
    }
}
