package cn.xiaozhou233.juicemixin.operation;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The code of a mixin handler, cloned and prepared for injection into a target
 * method.
 * <p>
 * A body owns a fresh {@link InsnList}: it can only be inserted into one target
 * method. Use {@link #copy()} for every additional insertion, otherwise the
 * instruction list is re-linked and the first injection is destroyed.
 */
public final class InjectionBody {

    private final InsnList instructions;
    private final List<TryCatchBlockNode> tryCatchBlocks;

    public InjectionBody(InsnList instructions, List<TryCatchBlockNode> tryCatchBlocks) {
        this.instructions = instructions;
        this.tryCatchBlocks = Collections.unmodifiableList(tryCatchBlocks);
    }

    public InsnList getInstructions() {
        return instructions;
    }

    public List<TryCatchBlockNode> getTryCatchBlocks() {
        return tryCatchBlocks;
    }

    /**
     * Creates an independent copy of this body, including labels and try-catch
     * ranges. Used when one handler is injected at several matched
     * instructions.
     *
     * @return a new body that can be inserted separately
     */
    public InjectionBody copy() {
        Map<LabelNode, LabelNode> labels = new HashMap<>();

        for (AbstractInsnNode instruction : instructions) {
            if (instruction instanceof LabelNode) {
                labels.put((LabelNode) instruction, new LabelNode());
            }
        }

        InsnList copiedInstructions = new InsnList();
        for (AbstractInsnNode instruction : instructions) {
            copiedInstructions.add(instruction.clone(labels));
        }

        List<TryCatchBlockNode> copiedBlocks = new ArrayList<>();
        for (TryCatchBlockNode block : tryCatchBlocks) {
            copiedBlocks.add(new TryCatchBlockNode(
                    labels.get(block.start),
                    labels.get(block.end),
                    labels.get(block.handler),
                    block.type
            ));
        }

        return new InjectionBody(copiedInstructions, copiedBlocks);
    }
}
