package cn.xiaozhou233.juicemixin.internal.injection;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

/**
 * Matches {@code LDC} constants by their textual value.
 */
public final class ConstantInjectionPoint extends MemberInjectionPoint {

    @Override
    protected boolean matches(AbstractInsnNode instruction, String target) {
        if (!(instruction instanceof LdcInsnNode)) {
            return false;
        }

        return target.isEmpty()
                || matchesConstant(((LdcInsnNode) instruction).cst, target);
    }

    private static boolean matchesConstant(Object constant, String target) {
        if (constant instanceof String) {
            return target.equals(constant);
        }

        if (constant instanceof Type) {
            Type type = (Type) constant;

            if (target.equals(type.getDescriptor()) || target.equals(type.getClassName())) {
                return true;
            }

            return type.getSort() == Type.OBJECT && target.equals(type.getInternalName());
        }

        return target.equals(String.valueOf(constant));
    }
}
