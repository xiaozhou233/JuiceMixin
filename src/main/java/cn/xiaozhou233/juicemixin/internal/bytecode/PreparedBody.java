package cn.xiaozhou233.juicemixin.internal.bytecode;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A cloned handler body that is ready to be inserted into a target method.
 * <p>
 * A body owns a fresh {@link InsnList}. Insert it once directly; use
 * {@link #copy()} for every additional insertion.
 */
public final class PreparedBody {

    private final InsnList instructions;
    private final List<TryCatchBlockNode> tryCatchBlocks;
    private final int parameterSlots;
    private final int localSlotCount;

    public PreparedBody(
            InsnList instructions,
            List<TryCatchBlockNode> tryCatchBlocks,
            int parameterSlots,
            int localSlotCount
    ) {
        this.instructions = instructions;
        this.tryCatchBlocks = Collections.unmodifiableList(tryCatchBlocks);
        this.parameterSlots = parameterSlots;
        this.localSlotCount = localSlotCount;
    }

    public InsnList instructions() {
        return instructions;
    }

    public List<TryCatchBlockNode> tryCatchBlocks() {
        return tryCatchBlocks;
    }

    /**
     * Number of local slots used by the handler receiver and its parameters.
     * These slots keep their index so handler parameters keep referring to target
     * method arguments.
     */
    public int parameterSlots() {
        return parameterSlots;
    }

    /**
     * Number of local slots the handler body declares after its parameters.
     */
    public int localSlotCount() {
        return localSlotCount;
    }

    /**
     * Moves body locals by {@code offset}, keeping receiver and parameter slots in
     * place. Called once per insertion with the slot range allocated by the
     * target method.
     */
    public void shiftLocalVariables(int offset) {
        if (offset == 0) {
            return;
        }

        for (AbstractInsnNode instruction = instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (instruction instanceof VarInsnNode) {
                VarInsnNode variable = (VarInsnNode) instruction;
                if (variable.var >= parameterSlots) {
                    variable.var += offset;
                }
            } else if (instruction instanceof IincInsnNode) {
                IincInsnNode increment = (IincInsnNode) instruction;
                if (increment.var >= parameterSlots) {
                    increment.var += offset;
                }
            }
        }
    }

    public AbstractInsnNode lastInstruction() {
        return instructions.getLast();
    }

    /**
     * Creates an independent copy, including labels and try-catch ranges.
     *
     * @return a new body that can be inserted separately
     */
    public PreparedBody copy() {
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

        return new PreparedBody(
                copiedInstructions, copiedBlocks, parameterSlots, localSlotCount
        );
    }
}
