package cn.xiaozhou233.juicemixin.internal.bytecode;

import cn.xiaozhou233.juicemixin.exception.MixinException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * ASM class parsing helpers.
 */
public final class ClassNodes {

    private ClassNodes() {
    }

    public static ClassNode read(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new MixinException("Cannot parse class bytecode: input is empty");
        }

        try {
            ClassNode node = new ClassNode();
            new ClassReader(bytes).accept(node, 0);
            return node;
        } catch (RuntimeException e) {
            throw new MixinException("Cannot parse class bytecode", e);
        }
    }
}
