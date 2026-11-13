package cn.xiaozhou233.juicemixin.internal.metadata;

import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A parsed mixin class and the target it is applied to.
 */
public final class MixinDefinition {

    private final Class<?> mixinClass;
    private final ClassNode classNode;
    private final Class<?> targetClass;
    private final String targetInternalName;
    private final List<HandlerDefinition> handlers;

    public MixinDefinition(
            Class<?> mixinClass,
            ClassNode classNode,
            Class<?> targetClass,
            String targetInternalName,
            List<HandlerDefinition> handlers
    ) {
        this.mixinClass = Objects.requireNonNull(mixinClass, "mixinClass");
        this.classNode = Objects.requireNonNull(classNode, "classNode");
        this.targetClass = Objects.requireNonNull(targetClass, "targetClass");
        this.targetInternalName = Objects.requireNonNull(
                targetInternalName, "targetInternalName"
        );
        this.handlers = Collections.unmodifiableList(handlers);
    }

    public Class<?> mixinClass() {
        return mixinClass;
    }

    public ClassNode classNode() {
        return classNode;
    }

    public Class<?> targetClass() {
        return targetClass;
    }

    public String targetInternalName() {
        return targetInternalName;
    }

    public List<HandlerDefinition> handlers() {
        return handlers;
    }
}
