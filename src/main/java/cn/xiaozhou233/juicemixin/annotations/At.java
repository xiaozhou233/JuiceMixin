
package cn.xiaozhou233.juicemixin.annotations;

import cn.xiaozhou233.juicemixin.utils.AnnotationUtils;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.*;

/**
 * Describes an injection point. It may only be used as the value of
 * {@link Inject#at()} and cannot annotate any declaration.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface At {

    /** Injection point type; defaults to {@code HEAD}. */
    String value() default "HEAD";

    /**
     * Signature of the target member. Only used by INVOKE, FIELD and CONSTANT.
     * The format is {@code "owner.nameDescriptor"}, for example
     * {@code "java/io/PrintStream.println(Ljava/lang/String;)V"}.
     */
    String target() default "";

    /**
     * Which match to use. {@code -1} means all matches; {@code >= 0} selects only
     * the N-th match (zero-based).
     */
    int ordinal() default -1;

    /**
     * Whether to insert before or after the matched instruction. Only used by
     * INVOKE, FIELD and CONSTANT.
     */
    Shift shift() default Shift.BEFORE;

    enum Shift {
        BEFORE,
        AFTER
    }

    class Helper {

        public static At getAnnotation(AnnotationNode node) {
            if (!AnnotationUtils.isAnnotation(node, At.class)) {
                return null;
            }

            String value = getValue(node, "value", "HEAD");
            String target = getValue(node, "target", "");
            int ordinal = getValue(node, "ordinal", -1);
            Shift shift = getEnumValue(node, "shift", Shift.class, Shift.BEFORE);

            return new At() {
                @Override
                public String value() {
                    return value;
                }

                @Override
                public String target() {
                    return target;
                }

                @Override
                public int ordinal() {
                    return ordinal;
                }

                @Override
                public Shift shift() {
                    return shift;
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return At.class;
                }
            };
        }

        private static <T> T getValue(
                AnnotationNode node, String name, T defaultValue
        ) {
            T value = AnnotationUtils.getAnnotationValue(node, name);
            return value != null ? value : defaultValue;
        }

        private static <E extends Enum<E>> E getEnumValue(
                AnnotationNode node,
                String name,
                Class<E> enumClass,
                E defaultValue
        ) {
            String[] value = AnnotationUtils.getAnnotationValue(node, name);

            if (value == null || value.length < 2) {
                return defaultValue;
            }

            return Enum.valueOf(enumClass, value[1]);
        }

        public static At getDefaultAnnotation() {
            return new At() {
                @Override
                public String value() {
                    return "HEAD";
                }

                @Override
                public String target() {
                    return "";
                }

                @Override
                public int ordinal() {
                    return -1;
                }

                @Override
                public Shift shift() {
                    return Shift.BEFORE;
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return At.class;
                }
            };
        }
    }
}
