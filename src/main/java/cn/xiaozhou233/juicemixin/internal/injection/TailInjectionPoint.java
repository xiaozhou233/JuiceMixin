package cn.xiaozhou233.juicemixin.internal.injection;

import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.internal.bytecode.TargetContext;
import cn.xiaozhou233.juicemixin.internal.metadata.InjectionSpec;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.List;

/**
 * Inserts code before the last return instruction of the original target
 * method.
 */
public final class TailInjectionPoint implements InjectionPoint {

    @Override
    public List<Insertion> find(
            TargetContext context,
            MethodNode targetMethod,
            InjectionSpec spec
    ) {
        AbstractInsnNode lastReturn = null;

        for (AbstractInsnNode instruction = targetMethod.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (!context.originalInstructions().contains(instruction)) {
                continue;
            }

            if (isReturn(instruction.getOpcode())) {
                lastReturn = instruction;
            }
        }

        if (lastReturn == null) {
            throw new MixinException(
                    "Cannot inject at TAIL: target method " + targetMethod.name
                            + targetMethod.desc + " has no return instruction"
            );
        }

        return Collections.singletonList(Insertion.before(lastReturn));
    }

    private static boolean isReturn(int opcode) {
        return opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
    }
}
