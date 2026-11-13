package cn.xiaozhou233.juicemixin.annotations;

import cn.xiaozhou233.juicemixin.utils.AnnotationUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a mixin and declares the class its code is mixed into.
 * <p>
 * Mixing happens at runtime, so the target class must already be loaded and
 * resolvable by the context class loader when the mixin is processed. The
 * annotation therefore cannot be used to declare a target that does not exist
 * (yet) in the running JVM.
 * <p>
 * Retention is {@link RetentionPolicy#RUNTIME} so the annotation stays readable
 * through reflection as well; the transformer itself reads the bytecode form.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mixin {

    /**
     * The class to mix into. It is read back from the bytecode as a {@link Type}
     * and then resolved through the context class loader, which is why the
     * target has to be loadable at transform time.
     *
     * @return the target class
     */
    Class<?> value();

    /**
     * Helper for reading {@link Mixin} from a {@link ClassNode}.
     * <p>
     * The returned instance is a lightweight anonymous implementation rebuilt
     * from the bytecode, not the annotation proxy the JVM would create. It
     * therefore does not implement the {@code equals}/{@code hashCode}/
     * {@code toString} contract of {@link java.lang.annotation.Annotation};
     * do not use it as a map key and do not compare it with a reflective
     * annotation instance.
     */
    class Helper {

        /** Prevents instantiation: this class only exposes static helpers. */
        private Helper() {
        }

        /**
         * Reads and resolves the {@link Mixin} annotation of a mixin class.
         *
         * @param node the {@link ClassNode} of the mixin class
         * @return the resolved annotation, or {@code null} if the class is not
         *         annotated with {@link Mixin}
         * @throws IllegalStateException if the annotation carries no target class
         *                               or the target class cannot be loaded by
         *                               the context class loader
         */
        public static Mixin getAnnotation(ClassNode node) {
            AnnotationNode annotation = AnnotationUtils.getAnnotation(node, Mixin.class);
            if (annotation == null) {
                return null;
            }

            Type type = AnnotationUtils.getAnnotationValue(annotation, "value");
            if (type == null) {
                throw new IllegalStateException("Mixin annotation has no target class");
            }

            String name = type.getClassName();
            Class<?> target;
            try {
                target = load(name);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "Cannot load mixin target: " + name, e
                );
            }

            return new Mixin() {
                @Override
                public Class<?> value() {
                    return target;
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return Mixin.class;
                }
            };
        }

        /**
         * Loads the target through the context class loader, falling back to the
         * loader of this library. The fallback keeps the target resolvable in
         * plain JUnit/standalone runs where the context class loader is not the
         * application class loader.
         */
        private static Class<?> load(String name) throws ClassNotFoundException {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

            if (contextLoader != null) {
                try {
                    return Class.forName(name, false, contextLoader);
                } catch (ClassNotFoundException ignored) {
                    // Try the library class loader below.
                }
            }

            return Class.forName(name, false, Mixin.Helper.class.getClassLoader());
        }
    }

}
