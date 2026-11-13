package cn.xiaozhou233.juicemixin.internal.transform;

import cn.xiaozhou233.juicemixin.MixinConfig;
import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.internal.bytecode.ClassNodes;
import cn.xiaozhou233.juicemixin.internal.bytecode.FrameClassWriter;
import cn.xiaozhou233.juicemixin.internal.bytecode.TargetContext;
import cn.xiaozhou233.juicemixin.internal.metadata.MixinDefinition;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.Objects;

/**
 * Transforms one target class by applying all mixins registered for it.
 */
public final class TargetTransformer {

    private final MixinConfig config;

    public TargetTransformer(MixinConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public byte[] transform(List<MixinDefinition> mixins) {
        if (mixins == null || mixins.isEmpty()) {
            throw new MixinException("No mixins were provided for a target class");
        }

        MixinDefinition first = mixins.get(0);
        String targetInternalName = first.targetInternalName();
        Class<?> targetClass = first.targetClass();

        byte[] targetBytes = config.bytecodeProvider().get(targetClass);
        if (targetBytes == null || targetBytes.length == 0) {
            throw new MixinException(
                    "Cannot read bytecode of target " + targetClass.getName()
            );
        }

        ClassNode targetNode = ClassNodes.read(targetBytes);
        TargetContext context = new TargetContext(targetNode);
        MixinApplicator applicator = new MixinApplicator(config);

        for (MixinDefinition mixin : mixins) {
            applicator.apply(context, mixin);
        }

        ClassWriter writer = new FrameClassWriter(config.classProvider());
        targetNode.accept(writer);
        byte[] transformed = writer.toByteArray();

        config.outputProvider().output(targetInternalName, transformed);
        config.loggerProvider().info(
                "Transformed target {} with {} mixin(s)",
                targetInternalName,
                mixins.size()
        );

        return transformed;
    }
}
