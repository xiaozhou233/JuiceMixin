package cn.xiaozhou233.juicemixin;

import cn.xiaozhou233.juicemixin.contents.MixinContext;
import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.processor.MixinProcessor;
import cn.xiaozhou233.juicemixin.registries.MixinRegistry;
import cn.xiaozhou233.juicemixin.utils.ASMUtils;
import cn.xiaozhou233.juicemixin.utils.MixinClassWriter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Applies every registered mixin to its target class.
 * <p>
 * Mixins are grouped by target, so several mixins of the same class are applied
 * to one target class node and written out once. The previous implementation
 * processed every mixin against its own copy of the target and overwrote the
 * output, which silently discarded all but the last mixin.
 */
public class Transformer {

    private final Map<String, List<MixinContext>> mixinsByTarget = new LinkedHashMap<>();
    private final Map<String, Class<?>> targetClasses = new LinkedHashMap<>();
    private boolean enabledOutputProvider = false;

    public Transformer(MixinRegistry mixinRegistry) {
        Objects.requireNonNull(mixinRegistry, "mixinRegistry");

        for (Class<?> mixin : mixinRegistry.getMixinList()) {
            byte[] mixinBytes = JuiceMixin.getBytecodeProvider().get(mixin);
            if (mixinBytes == null || mixinBytes.length == 0) {
                throw new MixinException(
                        "Cannot read the bytecode of mixin " + mixin.getName()
                );
            }

            MixinContext context = MixinContext.fromBytecode(mixinBytes);
            mixinsByTarget
                    .computeIfAbsent(context.getTargetInternalName(), key -> new ArrayList<>())
                    .add(context);
            targetClasses.putIfAbsent(context.getTargetInternalName(), context.getTargetClass());
        }
    }

    public Transformer setOutputProviderEnabled(boolean enabled) {
        this.enabledOutputProvider = enabled;
        return this;
    }

    /**
     * Transforms every target class.
     *
     * @return modified classes keyed by their internal name
     * @throws MixinException if a target cannot be loaded, parsed or written
     */
    public Map<String, byte[]> transform() {
        Map<String, byte[]> transformed = new LinkedHashMap<>();

        for (Map.Entry<String, List<MixinContext>> entry : mixinsByTarget.entrySet()) {
            String targetInternalName = entry.getKey();
            Class<?> targetClass = targetClasses.get(targetInternalName);

            ClassNode targetNode = loadTarget(targetClass);
            Set<AbstractInsnNode> originalInstructions = collectInstructions(targetNode);

            for (MixinContext context : entry.getValue()) {
                new MixinProcessor(context).process(targetNode, originalInstructions);
            }

            byte[] modifiedBytes = write(targetNode, targetClass);

            if (enabledOutputProvider) {
                JuiceMixin.getOutputProvider().output(targetInternalName, modifiedBytes);
            }

            transformed.put(targetInternalName, modifiedBytes);
        }

        return transformed;
    }

    /**
     * Snapshots the instructions of the original target class. Injection points
     * are matched against this snapshot only, so code injected by another mixin
     * is never matched again by a later mixin.
     */
    private static Set<AbstractInsnNode> collectInstructions(ClassNode targetNode) {
        Set<AbstractInsnNode> instructions =
                Collections.newSetFromMap(new IdentityHashMap<AbstractInsnNode, Boolean>());

        for (MethodNode method : targetNode.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                instructions.add(instruction);
            }
        }

        return instructions;
    }

    private static ClassNode loadTarget(Class<?> targetClass) {        byte[] targetBytes = JuiceMixin.getBytecodeProvider().get(targetClass);
        if (targetBytes == null || targetBytes.length == 0) {
            throw new MixinException(
                    "Cannot read the bytecode of target " + targetClass.getName()
            );
        }

        ClassNode node = ASMUtils.toClassNode(targetBytes);
        if (node == null) {
            throw new MixinException(
                    "Cannot parse the bytecode of target " + targetClass.getName()
            );
        }

        return node;
    }

    private static byte[] write(ClassNode targetNode, Class<?> targetClass) {
        ClassWriter writer = new MixinClassWriter(
                ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                targetClass.getClassLoader()
        );
        targetNode.accept(writer);
        return writer.toByteArray();
    }
}
