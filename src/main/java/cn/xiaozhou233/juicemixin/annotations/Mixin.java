package cn.xiaozhou233.juicemixin.annotations;

import cn.xiaozhou233.juicemixin.utils.AnnotationUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.lang.annotation.*;

/**
 * Declares the current class as a mixin for the target class.
 *
 * <p>The annotated class must:
 * <ul>
 *   <li>be an ordinary {@code public}, non-{@code abstract} class;</li>
 *   <li>provide a no-arg constructor. The framework creates and caches one mixin
 *       instance for <b>every target instance</b>.</li>
 * </ul>
 *
 * <p>Therefore a mixin's member fields hold state that follows the target
 * instance; different target instances do not affect each other.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mixin {

    /**
     * The target class.
     */
    Class<?> value();


    class Helper {

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
                target = Class.forName(
                        name,
                        false,
                        Thread.currentThread().getContextClassLoader()
                );
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
    }

}
