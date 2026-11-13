package cn.xiaozhou233.juicemixin.contents;

import cn.xiaozhou233.juicemixin.annotations.Mixin;
import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.utils.ASMUtils;
import lombok.Getter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.Objects;

/**
 * Holds a parsed mixin class together with the class it is mixed into.
 * <p>
 * The target class is only resolved to its internal name; its bytecode is
 * loaded and parsed by {@link cn.xiaozhou233.juicemixin.Transformer}. Keeping
 * the target node out of the context allows several mixins of the same target
 * to share one target class node.
 */
@Getter
public class MixinContext {
    private final ClassNode mixinNode;
    private final Class<?> targetClass;
    private final String targetInternalName;

    public MixinContext(ClassNode mixinNode) {
        this.mixinNode = Objects.requireNonNull(mixinNode, "mixinNode");

        Mixin mixin = Mixin.Helper.getAnnotation(mixinNode);
        if (mixin == null) {
            throw new MixinException(
                    "Class " + mixinNode.name + " is not annotated with @Mixin"
            );
        }

        this.targetClass = mixin.value();
        this.targetInternalName = Type.getInternalName(targetClass);
    }

    /**
     * Convenience factory that parses mixin bytecode and wraps it in a context.
     *
     * @param mixinBytes bytecode of the mixin class
     * @return the parsed context
     * @throws MixinException if the bytecode is empty or cannot be parsed
     */
    public static MixinContext fromBytecode(byte[] mixinBytes) {
        ClassNode node = ASMUtils.toClassNode(mixinBytes);
        if (node == null) {
            throw new MixinException("Cannot parse mixin bytecode: empty input");
        }
        return new MixinContext(node);
    }
}
