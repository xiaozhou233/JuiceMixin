package cn.xiaozhou233.juicemixin.internal.bytecode;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Mutable state of one target class while mixins are being applied.
 */
public final class TargetContext {

    private final ClassNode targetNode;
    private final OriginalInstructions originalInstructions;
    private final Map<MethodNode, LocalSlotAllocator> localAllocators =
            new IdentityHashMap<>();
    private final Map<MethodNode, AbstractInsnNode> headCursors = new IdentityHashMap<>();

    public TargetContext(ClassNode targetNode) {
        this.targetNode = Objects.requireNonNull(targetNode, "targetNode");
        this.originalInstructions = OriginalInstructions.snapshot(targetNode);
    }

    public ClassNode targetNode() {
        return targetNode;
    }

    public OriginalInstructions originalInstructions() {
        return originalInstructions;
    }

    public LocalSlotAllocator allocatorFor(MethodNode method) {
        return localAllocators.computeIfAbsent(
                method,
                targetMethod -> new LocalSlotAllocator(targetMethod.maxLocals)
        );
    }

    public AbstractInsnNode headCursor(MethodNode method) {
        return headCursors.get(method);
    }

    public void headCursor(MethodNode method, AbstractInsnNode lastInserted) {
        headCursors.put(method, lastInserted);
    }
}
