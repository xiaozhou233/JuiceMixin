package cn.xiaozhou233.juicemixin.internal.bytecode;

import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.provider.ClassProvider;
import org.objectweb.asm.ClassWriter;

import java.util.Objects;

/**
 * Computes stack map frames against the configured {@link ClassProvider}, so
 * classes of the transformed application are visible while ASM looks up common
 * super classes.
 */
public final class FrameClassWriter extends ClassWriter {

    private final ClassProvider classProvider;

    public FrameClassWriter(ClassProvider classProvider) {
        super(COMPUTE_FRAMES | COMPUTE_MAXS);
        this.classProvider = Objects.requireNonNull(classProvider, "classProvider");
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        Class<?> class1 = resolve(type1);
        Class<?> class2 = resolve(type2);

        if (class1.isAssignableFrom(class2)) {
            return type1;
        }
        if (class2.isAssignableFrom(class1)) {
            return type2;
        }
        if (class1.isInterface() || class2.isInterface()) {
            return "java/lang/Object";
        }

        do {
            class1 = class1.getSuperclass();
            if (class1 == null) {
                return "java/lang/Object";
            }
        } while (!class1.isAssignableFrom(class2));

        return class1.getName().replace('.', '/');
    }

    private Class<?> resolve(String internalName) {
        Class<?> resolved = classProvider.get(internalName);
        if (resolved == null) {
            throw new MixinException(
                    "Cannot resolve class while computing stack map frames: " + internalName
            );
        }
        return resolved;
    }
}
