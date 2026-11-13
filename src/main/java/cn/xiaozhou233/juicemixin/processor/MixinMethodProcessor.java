package cn.xiaozhou233.juicemixin.processor;

import cn.xiaozhou233.juicemixin.contents.MixinMethodContent;
import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.operation.InjectionBody;
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
 * Turns the body of a mixin handler into an {@link InjectionBody}.
 * <p>
 * The body is cloned so the handler itself is never modified. Debug metadata
 * (line numbers, local variable tables and stack map frames) is dropped: it
 * describes the mixin method, not the place the code is injected into. Stack map
 * frames are recomputed by the {@code ClassWriter} after all injections are
 * applied.
 */
public class MixinMethodProcessor {
    private final MixinMethodContent content;

    public MixinMethodProcessor(MixinMethodContent content) {
        this.content = content;
    }

    /**
     * Clones the handler body and removes its terminal return instruction, so
     * the code falls through into the rest of the target method.
     * <p>
     * Local variables declared inside the handler (and not handler parameters)
     * are moved by {@code localOffset} slots. Without this, a local would reuse
     * a target local slot and silently overwrite a parameter of the target
     * method.
     *
     * @param localOffset number of local slots already used by the target
     *                    method; must not be negative
     * @return a fresh body that can be inserted into exactly one target method
     * @throws MixinException if the handler cannot be injected as-is
     */
    public InjectionBody prepare(int localOffset) {
        if (localOffset < 0) {
            throw new MixinException("Local variable offset must not be negative: " + localOffset);
        }

        MethodNode mixinMethod = content.getMixinMethod();
        validate(mixinMethod);

        Map<LabelNode, LabelNode> labels = new HashMap<>();
        for (AbstractInsnNode instruction : mixinMethod.instructions) {
            if (instruction instanceof LabelNode) {
                labels.put((LabelNode) instruction, new LabelNode());
            }
        }

        InsnList instructions = new InsnList();
        AbstractInsnNode lastReturn = null;

        for (AbstractInsnNode instruction : mixinMethod.instructions) {
            if (isDebugMetadata(instruction)) {
                continue;
            }

            AbstractInsnNode cloned = instruction.clone(labels);
            instructions.add(cloned);

            if (isReturn(cloned.getOpcode())) {
                lastReturn = cloned;
            }
        }

        if (lastReturn == null) {
            throw new MixinException(
                    "Mixin method " + mixinMethod.name + mixinMethod.desc
                            + " has no return instruction and cannot be injected"
            );
        }

        instructions.remove(lastReturn);
        shiftLocalVariables(
                instructions,
                handlerParameterSlots(mixinMethod),
                localOffset
        );

        return new InjectionBody(instructions, cloneTryCatchBlocks(mixinMethod, labels));
    }

    private static void validate(MethodNode mixinMethod) {
        if ((mixinMethod.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
            throw new MixinException(
                    "Mixin method " + mixinMethod.name + mixinMethod.desc
                            + " must have a body (it cannot be abstract or native)"
            );
        }

        if (Type.getReturnType(mixinMethod.desc).getSort() != Type.VOID) {
            throw new MixinException(
                    "Mixin method " + mixinMethod.name + mixinMethod.desc
                            + " must return void, because its body is merged into the target method"
            );
        }

        if (mixinMethod.instructions == null || mixinMethod.instructions.size() == 0) {
            throw new MixinException(
                    "Mixin method " + mixinMethod.name + mixinMethod.desc + " has an empty body"
            );
        }
    }

    private static List<TryCatchBlockNode> cloneTryCatchBlocks(
            MethodNode mixinMethod,
            Map<LabelNode, LabelNode> labels
    ) {
        List<TryCatchBlockNode> cloned = new ArrayList<>();

        if (mixinMethod.tryCatchBlocks == null) {
            return cloned;
        }

        for (TryCatchBlockNode block : mixinMethod.tryCatchBlocks) {
            LabelNode start = labels.get(block.start);
            LabelNode end = labels.get(block.end);
            LabelNode handler = labels.get(block.handler);

            if (start == null || end == null || handler == null) {
                throw new MixinException(
                        "Mixin method " + mixinMethod.name + mixinMethod.desc
                                + " has a try-catch block that references an unknown label"
                );
            }

            cloned.add(new TryCatchBlockNode(start, end, handler, block.type));
        }

        return cloned;
    }

    /**
     * Number of local slots occupied by the hidden {@code this} and the declared
     * parameters of the handler. These slots intentionally keep their index so
     * handler parameters keep referring to the target method arguments.
     */
    private static int handlerParameterSlots(MethodNode method) {
        int slots = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;

        for (Type argument : Type.getArgumentTypes(method.desc)) {
            slots += argument.getSize();
        }

        return slots;
    }

    private static void shiftLocalVariables(InsnList instructions, int from, int offset) {
        if (offset == 0) {
            return;
        }

        for (AbstractInsnNode instruction = instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (instruction instanceof VarInsnNode) {
                VarInsnNode variable = (VarInsnNode) instruction;
                if (variable.var >= from) {
                    variable.var += offset;
                }
            } else if (instruction instanceof IincInsnNode) {
                IincInsnNode increment = (IincInsnNode) instruction;
                if (increment.var >= from) {
                    increment.var += offset;
                }
            }
        }
    }

    /**
     * Line numbers and stack map frames are bytecode metadata, not executable
     * instructions.
     */
    private static boolean isDebugMetadata(AbstractInsnNode instruction) {
        return instruction instanceof LineNumberNode
                || instruction instanceof FrameNode;
    }

    private static boolean isReturn(int opcode) {
        return opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
    }
}
