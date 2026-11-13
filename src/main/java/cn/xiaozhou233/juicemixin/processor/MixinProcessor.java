package cn.xiaozhou233.juicemixin.processor;

import cn.xiaozhou233.juicemixin.JuiceMixin;
import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.contents.MixinContext;
import cn.xiaozhou233.juicemixin.contents.MixinMethodContent;
import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.operation.InjectOperation;
import cn.xiaozhou233.juicemixin.operation.InjectionBody;
import cn.xiaozhou233.juicemixin.provider.LoggerProvider;
import cn.xiaozhou233.juicemixin.utils.AnnotationUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Set;

/**
 * Applies every handler of one mixin to a target class node.
 */
public class MixinProcessor {

    private final MixinContext context;

    public MixinProcessor(MixinContext context) {
        this.context = context;
    }

    /**
     * Applies all {@code @Inject} handlers of the mixin to {@code targetNode}.
     * The same node may be passed to several processors, which is how multiple
     * mixins of the same target are combined.
     *
     * @param targetNode the class node of the mixin target
     * @param originalInstructions identity set of the instructions that belong
     *                             to the original target class; injected code is
     *                             not matched again
     */
    public void process(ClassNode targetNode, Set<AbstractInsnNode> originalInstructions) {
        LoggerProvider logger = JuiceMixin.getLoggerProvider();

        logger.info(
                "Processing mixin {} for target {}",
                context.getMixinNode().name,
                context.getTargetInternalName()
        );

        for (MethodNode mixinMethod : context.getMixinNode().methods) {
            if (!AnnotationUtils.hasAnnotation(mixinMethod, Inject.class)
                    && !AnnotationUtils.hasAnnotation(mixinMethod, Inject.List.class)) {
                logger.debug(
                        "Skipping method {}{} as it is not annotated with @Inject",
                        mixinMethod.name,
                        mixinMethod.desc
                );
                continue;
            }

            MixinMethodContent content = new MixinMethodContent(mixinMethod);

            for (Inject inject : content.getInjects()) {
                MethodNode targetMethod = findTargetMethod(targetNode, inject, mixinMethod);
                validateCompatibility(targetMethod, mixinMethod);

                InjectionBody body = new MixinMethodProcessor(content)
                        .prepare(targetMethod.maxLocals);

                InjectOperation.execute(
                        targetMethod, inject.at(), body, originalInstructions
                );

                logger.debug(
                        "Injected {} into {}{} at {}",
                        mixinMethod.name,
                        targetMethod.name,
                        targetMethod.desc,
                        inject.at().value()
                );
            }
        }
    }

    private MethodNode findTargetMethod(
            ClassNode targetNode,
            Inject inject,
            MethodNode mixinMethod
    ) {
        for (MethodNode method : targetNode.methods) {
            if (method.name.equals(inject.method()) && method.desc.equals(inject.desc())) {
                return method;
            }
        }

        throw new MixinException(
                "Target method " + inject.method() + inject.desc() + " not found in "
                        + context.getTargetInternalName() + " (required by mixin method "
                        + mixinMethod.name + mixinMethod.desc + ")"
        );
    }

    private static void validateCompatibility(MethodNode targetMethod, MethodNode mixinMethod) {
        if ((targetMethod.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
            throw new MixinException(
                    "Cannot inject into abstract or native target method "
                            + targetMethod.name + targetMethod.desc
            );
        }

        boolean targetStatic = (targetMethod.access & Opcodes.ACC_STATIC) != 0;
        boolean mixinStatic = (mixinMethod.access & Opcodes.ACC_STATIC) != 0;

        if (targetStatic != mixinStatic) {
            throw new MixinException(
                    "Mixin method " + mixinMethod.name + mixinMethod.desc
                            + " and target method " + targetMethod.name + targetMethod.desc
                            + " must both be static or both be instance methods"
            );
        }

        Type[] targetArguments = Type.getArgumentTypes(targetMethod.desc);
        Type[] mixinArguments = Type.getArgumentTypes(mixinMethod.desc);

        if (mixinArguments.length > targetArguments.length) {
            throw new MixinException(
                    "Mixin method " + mixinMethod.name + mixinMethod.desc
                            + " declares more parameters than target method "
                            + targetMethod.name + targetMethod.desc
            );
        }

        for (int i = 0; i < mixinArguments.length; i++) {
            if (!mixinArguments[i].equals(targetArguments[i])) {
                throw new MixinException(
                        "Parameter " + (i + 1) + " of mixin method "
                                + mixinMethod.name + mixinMethod.desc + " ("
                                + mixinArguments[i] + ") does not match target method "
                                + targetMethod.name + targetMethod.desc + " ("
                                + targetArguments[i] + ")"
                );
            }
        }

        if (Type.getReturnType(targetMethod.desc).getSort() != Type.VOID
                && countReturns(mixinMethod) > 1) {
            throw new MixinException(
                    "Mixin method " + mixinMethod.name + mixinMethod.desc
                            + " returns early, which cannot be injected into non-void target "
                            + targetMethod.name + targetMethod.desc
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
}
