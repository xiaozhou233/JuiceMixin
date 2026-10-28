package cn.xiaozhou233.juicemixin;

import org.objectweb.asm.tree.ClassNode;

public class MixinContext {
    private final ClassNode mixinNode;
    public MixinContext(ClassNode mixinNode) {
        this.mixinNode = mixinNode;
    }
}
