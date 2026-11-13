package cn.xiaozhou233.juicemixin.internal.metadata;

import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.internal.bytecode.MethodRef;

import java.util.Objects;

/**
 * One parsed {@code @Inject} annotation.
 */
public final class InjectionSpec {

    private final InjectionPointKind kind;
    private final MethodRef targetMethod;
    private final String target;
    private final int ordinal;
    private final InsertionShift shift;

    public InjectionSpec(
            InjectionPointKind kind,
            MethodRef targetMethod,
            String target,
            int ordinal,
            InsertionShift shift
    ) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.targetMethod = Objects.requireNonNull(targetMethod, "targetMethod");
        this.target = target == null ? "" : target;
        this.shift = Objects.requireNonNull(shift, "shift");

        if (ordinal < -1) {
            throw new MixinException("Ordinal must be -1 or greater, but was " + ordinal);
        }
        this.ordinal = ordinal;

        if (!kind.isMemberBased() && shift != InsertionShift.BEFORE) {
            throw new MixinException(
                    "Shift AFTER is not supported for " + kind + " injection"
            );
        }
    }

    public InjectionPointKind kind() {
        return kind;
    }

    public MethodRef targetMethod() {
        return targetMethod;
    }

    public String target() {
        return target;
    }

    public int ordinal() {
        return ordinal;
    }

    public InsertionShift shift() {
        return shift;
    }
}
