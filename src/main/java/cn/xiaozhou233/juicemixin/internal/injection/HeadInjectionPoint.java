package cn.xiaozhou233.juicemixin.internal.injection;

import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.internal.bytecode.TargetContext;
import cn.xiaozhou233.juicemixin.internal.metadata.InjectionSpec;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.List;

/**
 * Inserts code at the start of a method, or after the {@code super(...)} /
 * {@code this(...)} call in a constructor.
 * <p>
 * Several HEAD injections into the same method are chained through the target
 * context, so handlers run in mixin registration order.
 */
public final class HeadInjectionPoint implements InjectionPoint {

    @Override
    public List<Insertion> find(
            TargetContext context,
            MethodNode targetMethod,
            InjectionSpec spec
    ) {
        AbstractInsnNode cursor = context.headCursor(targetMethod);
        if (cursor != null) {
            return Collections.singletonList(Insertion.after(cursor));
        }

        if ("<init>".equals(targetMethod.name)) {
            // 'this' is uninitialized before the super(...) / this(...) call, so
            // injected code may only run after it.
            return Collections.singletonList(Insertion.after(constructorCall(targetMethod)));
        }

        AbstractInsnNode anchor = firstOriginalInstruction(context, targetMethod);
        return Collections.singletonList(Insertion.before(anchor));
    }

    private static AbstractInsnNode firstOriginalInstruction(
            TargetContext context,
            MethodNode targetMethod
    ) {
        for (AbstractInsnNode instruction = targetMethod.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (context.originalInstructions().contains(instruction)) {
                return instruction;
            }
        }

        throw new MixinException(
                "Cannot inject into target method " + targetMethod.name
                        + targetMethod.desc + ": it has no original instructions"
        );
    }

    private static AbstractInsnNode constructorCall(MethodNode constructor) {
        for (AbstractInsnNode instruction = constructor.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() == Opcodes.INVOKESPECIAL && "<init>".equals(call.name)) {
                return call;
            }
        }

        throw new MixinException(
                "Cannot inject at HEAD of constructor " + constructor.desc
                        + ": no super(...) or this(...) call found"
        );
    }
}
