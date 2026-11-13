package cn.xiaozhou233.juicemixin.internal.bytecode;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Identity snapshot of the instructions that were present in the target class
 * before any mixin was applied.
 * <p>
 * Injection points are matched only against this snapshot, so code injected by
 * one mixin is never matched again by another mixin.
 */
public final class OriginalInstructions {

    private final Set<AbstractInsnNode> instructions;

    private OriginalInstructions(ClassNode node) {
        Set<AbstractInsnNode> snapshot =
                Collections.newSetFromMap(new IdentityHashMap<AbstractInsnNode, Boolean>());

        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                snapshot.add(instruction);
            }
        }

        this.instructions = snapshot;
    }

    public static OriginalInstructions snapshot(ClassNode node) {
        return new OriginalInstructions(node);
    }

    public boolean contains(AbstractInsnNode instruction) {
        return instructions.contains(instruction);
    }
}
