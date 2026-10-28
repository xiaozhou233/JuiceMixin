package cn.xiaozhou233.juicemixin;

import cn.xiaozhou233.juicemixin.annotations.Mixin;
import cn.xiaozhou233.juicemixin.utils.ASMUtils;
import lombok.Getter;
import org.objectweb.asm.tree.ClassNode;

import java.util.Objects;

@Getter
public class MixinContext {
    private final ClassNode mixinNode;
    private final ClassNode targetNode;
    private final String targetInternalName;
    private final byte[] targetOriginalBytes;
    public MixinContext(ClassNode mixinNode) {
        this.mixinNode = mixinNode;

        Class<?> targetClass = Objects.requireNonNull(Mixin.Helper.getAnnotation(mixinNode)).value();
        targetInternalName = targetClass.getName().replace(".", "/");
        JuiceMixin.getLoggerProvider().info("Loading mixin {}, target {}", mixinNode.name, targetInternalName);
        targetOriginalBytes = JuiceMixin.getBytecodeProvider().get(targetClass);
        targetNode = ASMUtils.toClassNode(targetOriginalBytes);

        if (targetNode==null)
            JuiceMixin.getLoggerProvider().error("Failed to load mixin {}", targetClass.getName());
    }
}
