package cn.xiaozhou233.juicemixin.internal.metadata;

import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A mixin handler method together with all injection points declared on it.
 */
public final class HandlerDefinition {

    private final MethodNode method;
    private final List<InjectionSpec> injections;

    public HandlerDefinition(MethodNode method, List<InjectionSpec> injections) {
        this.method = Objects.requireNonNull(method, "method");
        this.injections = Collections.unmodifiableList(injections);
    }

    public MethodNode method() {
        return method;
    }

    public List<InjectionSpec> injections() {
        return injections;
    }
}
