package cn.xiaozhou233.juicemixin.internal.injection;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Matches regular method invocations by {@code owner.name(descriptor)}.
 * <p>
 * {@code invokedynamic} instructions have no owner and are intentionally not
 * matched.
 */
public final class InvokeInjectionPoint extends MemberInjectionPoint {

    @Override
    protected boolean matches(AbstractInsnNode instruction, String target) {
        if (!(instruction instanceof MethodInsnNode)) {
            return false;
        }

        MethodInsnNode call = (MethodInsnNode) instruction;
        return target.isEmpty()
                || target.equals(call.owner + "." + call.name + call.desc);
    }
}
