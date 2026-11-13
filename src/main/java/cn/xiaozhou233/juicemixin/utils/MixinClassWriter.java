package cn.xiaozhou233.juicemixin.utils;

import cn.xiaozhou233.juicemixin.exception.MixinException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

/**
 * A {@link ClassWriter} that computes stack map frames against the class loader
 * of the class being transformed.
 * <p>
 * The default implementation resolves common super classes with the class
 * loader of ASM itself, which cannot see the classes of the application that
 * uses JuiceMixin.
 */
public class MixinClassWriter extends ClassWriter {

    private final ClassLoader classLoader;

    public MixinClassWriter(int flags, ClassLoader classLoader) {
        super(flags);

        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        this.classLoader = classLoader;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        try {
            Class<?> class1 = load(type1);
            Class<?> class2 = load(type2);

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

            return Type.getInternalName(class1);
        } catch (ClassNotFoundException e) {
            throw new MixinException(
                    "Cannot compute a common super class for " + type1 + " and " + type2
                            + ": class not visible to " + classLoader,
                    e
            );
        }
    }

    private Class<?> load(String internalName) throws ClassNotFoundException {
        return Class.forName(internalName.replace('/', '.'), false, classLoader);
    }
}
