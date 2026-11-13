package cn.xiaozhou233.juicemixin.annotations;

import cn.xiaozhou233.juicemixin.utils.AnnotationUtils;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes an injection point: the kind of point, the member to match, the
 * match ordinal and where the injected code is placed relative to the match.
 * <p>
 * Only meaningful as a member of {@link Inject} and it cannot be applied on its
 * own, which is enforced by declaring {@code @Target} with an empty element list.
 * <p>
 * Example:
 * <pre>
 * &#64;Mixin(Target.class)
 * public class TargetMixin {
 *     &#64;Inject(method = "run", desc = "()V",
 *             at = &#64;At(value = "INVOKE",
 *                      target = "java/io/PrintStream.println(Ljava/lang/String;)V"))
 *     private void onRun() {
 *     }
 * }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface At {

    /**
     * The kind of injection point.
     * <ul>
     *   <li>{@code TAIL} - before the last {@code RETURN} instruction of the
     *       method (default).</li>
     *   <li>{@code HEAD} - before the first instruction of the method.</li>
     *   <li>{@code INVOKE} / {@code FIELD} / {@code CONSTANT} - target matching
     *       points, used together with {@link #target()} and {@link #ordinal()}.</li>
     * </ul>
     * The value is a plain string and is therefore not validated at compile time:
     * a typo or an unsupported name only shows up at runtime as "nothing matched".
     *
     * @return the injection point kind, {@code "TAIL"} by default
     */
    String value() default "TAIL";

    /**
     * Signature of the member to match, only used by {@code INVOKE}, {@code FIELD}
     * and {@code CONSTANT} points.
     * <p>
     * Format is {@code owner + "." + name + descriptor}, where {@code owner} is an
     * ASM internal name (slash separated, not a dotted Java name). For example
     * {@code "java/io/PrintStream.println(Ljava/lang/String;)V"}.
     * The default empty string means "no member restriction".
     *
     * @return the member signature, {@code ""} by default
     */
    String target() default "";

    /**
     * Zero based index of the match to use when the target occurs more than once
     * in the method, counted in instruction order. Only used by {@code INVOKE},
     * {@code FIELD} and {@code CONSTANT} points.
     * The default {@code -1} means "no ordinal restriction": every match is used.
     *
     * @return the zero based match index, {@code -1} by default
     */
    int ordinal() default -1;

    /**
     * Where the injected code is placed relative to the matched instruction.
     * Defaults to {@link Shift#BEFORE}.
     *
     * @return the placement, {@link Shift#BEFORE} by default
     */
    Shift shift() default Shift.BEFORE;

    /** Placement of the injected code relative to the matched instruction. */
    enum Shift {
        /** Insert before the matched instruction. */
        BEFORE,
        /** Insert after the matched instruction. */
        AFTER
    }

    /**
     * Helper for reading {@link At} annotations from bytecode.
     * <p>
     * Missing attributes fall back to the same values as the defaults declared on
     * this annotation (Java does not store defaults in the bytecode), so the values
     * returned here match what {@code @At} reports through reflection.
     * <p>
     * Returned instances are lightweight anonymous implementations, not annotation
     * proxies: they do not implement the {@code equals}/{@code hashCode}/
     * {@code toString} contract of {@link Annotation}.
     */
    class Helper {

        /** Prevents instantiation: this class only exposes static helpers. */
        private Helper() {
        }

        /** Shared instance used when the whole {@code at} attribute is absent. */
        private static final At DEFAULT = new At() {
            @Override
            public String value() {
                return "TAIL";
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

        /**
         * Parses an {@link At} annotation node, filling in declared defaults for
         * every attribute that is not present in the bytecode.
         *
         * @param node the annotation node to parse
         * @return the parsed annotation, or {@code null} if the node is not an
         *         {@link At} annotation
         * @throws IllegalArgumentException if the node carries an unknown
         *                                  {@link Shift} constant
         */
        public static At getAnnotation(AnnotationNode node) {
            if (!AnnotationUtils.isAnnotation(node, At.class)) {
                return null;
            }

            String value = getValue(node, "value", "TAIL");
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

        /**
         * Returns the annotation equivalent to a bare {@code @At}, i.e. one whose
         * attributes are all at their declared defaults.
         *
         * @return the shared default instance
         */
        public static At getDefaultAnnotation() {
            return DEFAULT;
        }

        private static <T> T getValue(
                AnnotationNode node, String name, T defaultValue
        ) {
            T value = AnnotationUtils.getAnnotationValue(node, name);
            return value != null ? value : defaultValue;
        }

        /**
         * Reads an enum attribute. ASM stores enum values as a two element array
         * holding the enum descriptor and the constant name, hence the second
         * element.
         */
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
    }
}
