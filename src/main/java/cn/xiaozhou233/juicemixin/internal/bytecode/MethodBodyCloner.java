package cn.xiaozhou233.juicemixin.internal.bytecode;

import cn.xiaozhou233.juicemixin.exception.MixinException;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clones a mixin handler body into a {@link PreparedBody}.
 * <p>
 * Labels and try-catch ranges are remapped, debug metadata is dropped and the
 * terminal return instruction is removed so the body falls through into the
 * target method.
 */
public final class MethodBodyCloner {

    private MethodBodyCloner() {
    }

    public static PreparedBody clone(MethodNode handler) {
        validate(handler);

        Map<LabelNode, LabelNode> labels = new HashMap<>();
        for (AbstractInsnNode instruction : handler.instructions) {
            if (instruction instanceof LabelNode) {
                labels.put((LabelNode) instruction, new LabelNode());
            }
        }

        int parameterSlots = parameterSlots(handler);
        int maxSlotEnd = parameterSlots;
        InsnList instructions = new InsnList();
        AbstractInsnNode lastReturn = null;

        for (AbstractInsnNode instruction : handler.instructions) {
            if (isDebugMetadata(instruction)) {
                continue;
            }

            if (instruction instanceof VarInsnNode) {
                VarInsnNode variable = (VarInsnNode) instruction;
                maxSlotEnd = Math.max(
                        maxSlotEnd,
                        variable.var + variableSize(variable.getOpcode())
                );
            } else if (instruction instanceof IincInsnNode) {
                IincInsnNode increment = (IincInsnNode) instruction;
                maxSlotEnd = Math.max(maxSlotEnd, increment.var + 1);
            }

            AbstractInsnNode cloned = instruction.clone(labels);
            instructions.add(cloned);

            if (isReturn(cloned.getOpcode())) {
                lastReturn = cloned;
            }
        }

        if (lastReturn == null) {
            throw new MixinException(
                    "Mixin method " + handler.name + handler.desc
                            + " has no return instruction and cannot be injected"
            );
        }

        instructions.remove(lastReturn);

        int localSlotCount = Math.max(0, maxSlotEnd - parameterSlots);
        return new PreparedBody(
                instructions,
                cloneTryCatchBlocks(handler, labels),
                parameterSlots,
                localSlotCount
        );
    }

    private static void validate(MethodNode handler) {
        if ((handler.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
            throw new MixinException(
                    "Mixin method " + handler.name + handler.desc
                            + " must have a body (it cannot be abstract or native)"
            );
        }

        if (Type.getReturnType(handler.desc).getSort() != Type.VOID) {
            throw new MixinException(
                    "Mixin method " + handler.name + handler.desc
                            + " must return void, because its body is merged into the target method"
            );
        }

        if (handler.instructions == null || handler.instructions.size() == 0) {
            throw new MixinException(
                    "Mixin method " + handler.name + handler.desc + " has an empty body"
            );
        }
    }

    private static int parameterSlots(MethodNode handler) {
        int slots = (handler.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;

        for (Type argument : Type.getArgumentTypes(handler.desc)) {
            slots += argument.getSize();
        }

        return slots;
    }

    private static int variableSize(int opcode) {
        switch (opcode) {
            case Opcodes.LLOAD:
            case Opcodes.LSTORE:
            case Opcodes.DLOAD:
            case Opcodes.DSTORE:
                return 2;
            default:
                return 1;
        }
    }

    private static List<TryCatchBlockNode> cloneTryCatchBlocks(
            MethodNode handler,
            Map<LabelNode, LabelNode> labels
    ) {
        List<TryCatchBlockNode> cloned = new ArrayList<>();

        if (handler.tryCatchBlocks == null) {
            return cloned;
        }

        for (TryCatchBlockNode block : handler.tryCatchBlocks) {
            LabelNode start = labels.get(block.start);
            LabelNode end = labels.get(block.end);
            LabelNode handlerLabel = labels.get(block.handler);

            if (start == null || end == null || handlerLabel == null) {
                throw new MixinException(
                        "Mixin method " + handler.name + handler.desc
                                + " has a try-catch block that references an unknown label"
                );
            }

            cloned.add(new TryCatchBlockNode(start, end, handlerLabel, block.type));
        }

        return cloned;
    }

    private static boolean isDebugMetadata(AbstractInsnNode instruction) {
        return instruction instanceof LineNumberNode
                || instruction instanceof FrameNode;
    }

    private static boolean isReturn(int opcode) {
        return opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
    }
}
