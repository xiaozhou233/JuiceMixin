package cn.xiaozhou233.juicemixin.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class ASMUtils {
    public static ClassNode toClassNode(byte[] bytes) {
        if (bytes != null && bytes.length > 0) {
            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            return node;
        }
        return null;
    }
}
