package cn.xiaozhou233.juicemixin.internal.bytecode;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Looks methods up by name and descriptor.
 */
public final class MethodLookup {

    private MethodLookup() {
    }

    public static MethodNode find(ClassNode node, MethodRef ref) {
        for (MethodNode method : node.methods) {
            if (method.name.equals(ref.name()) && method.desc.equals(ref.desc())) {
                return method;
            }
        }

        return null;
    }
}
