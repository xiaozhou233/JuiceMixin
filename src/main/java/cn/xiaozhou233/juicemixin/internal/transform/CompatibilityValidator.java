package cn.xiaozhou233.juicemixin.internal.transform;

import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.internal.metadata.HandlerDefinition;
import cn.xiaozhou233.juicemixin.internal.metadata.InjectionSpec;
import cn.xiaozhou233.juicemixin.internal.metadata.MixinDefinition;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Validates that a handler can be merged into a target method.
 */
public final class CompatibilityValidator {

    private CompatibilityValidator() {
    }

    public static void validate(
            MixinDefinition mixin,
            HandlerDefinition handler,
            MethodNode targetMethod,
            InjectionSpec spec
    ) {
        MethodNode handlerMethod = handler.method();

        if ((targetMethod.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
            throw new MixinException(
                    "Cannot inject into abstract or native target method "
                            + targetMethod.name + targetMethod.desc
                            + " (mixin " + mixin.mixinClass().getName() + ")"
            );
        }

        boolean targetStatic = (targetMethod.access & Opcodes.ACC_STATIC) != 0;
        boolean handlerStatic = (handlerMethod.access & Opcodes.ACC_STATIC) != 0;

        if (targetStatic != handlerStatic) {
            throw new MixinException(
                    "Mixin method " + describe(mixin, handlerMethod)
                            + " and target method " + targetMethod.name + targetMethod.desc
                            + " must both be static or both be instance methods"
            );
        }

        Type[] targetArguments = Type.getArgumentTypes(targetMethod.desc);
        Type[] handlerArguments = Type.getArgumentTypes(handlerMethod.desc);

        if (handlerArguments.length > targetArguments.length) {
            throw new MixinException(
                    "Mixin method " + describe(mixin, handlerMethod)
                            + " declares more parameters than target method "
                            + targetMethod.name + targetMethod.desc
            );
        }

        for (int i = 0; i < handlerArguments.length; i++) {
            if (!handlerArguments[i].equals(targetArguments[i])) {
                throw new MixinException(
                        "Parameter " + (i + 1) + " of mixin method "
                                + describe(mixin, handlerMethod) + " (" + handlerArguments[i]
                                + ") does not match target method " + targetMethod.name
                                + targetMethod.desc + " (" + targetArguments[i] + ")"
                );
            }
        }

        if (Type.getReturnType(targetMethod.desc).getSort() != Type.VOID
                && countReturns(handlerMethod) > 1) {
            throw new MixinException(
                    "Mixin method " + describe(mixin, handlerMethod)
                            + " returns early, which cannot be injected into non-void target "
                            + targetMethod.name + targetMethod.desc
                            + " at " + spec.kind()
            );
        }
    }

    private static int countReturns(MethodNode method) {
        int returns = 0;

        for (AbstractInsnNode instruction : method.instructions) {
            int opcode = instruction.getOpcode();
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                returns++;
            }
        }

        return returns;
    }

    private static String describe(MixinDefinition mixin, MethodNode method) {
        return mixin.mixinClass().getName() + "#" + method.name + method.desc;
    }
}
