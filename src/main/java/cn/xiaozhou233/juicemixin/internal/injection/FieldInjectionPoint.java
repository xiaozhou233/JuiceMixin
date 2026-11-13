package cn.xiaozhou233.juicemixin.internal.injection;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Matches field accesses by {@code owner.name:descriptor}.
 */
public final class FieldInjectionPoint extends MemberInjectionPoint {

    @Override
    protected boolean matches(AbstractInsnNode instruction, String target) {
        if (!(instruction instanceof FieldInsnNode)) {
            return false;
        }

        FieldInsnNode field = (FieldInsnNode) instruction;
        return target.isEmpty()
                || target.equals(field.owner + "." + field.name + ":" + field.desc);
    }
}
