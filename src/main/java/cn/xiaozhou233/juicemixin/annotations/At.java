package cn.xiaozhou233.juicemixin.annotations;

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
     *   <li>{@code TAIL} - before the last return instruction of the method
     *       (default).</li>
     *   <li>{@code HEAD} - before the first instruction of the method, or after
     *       the {@code super(...)} / {@code this(...)} call in a constructor.</li>
     *   <li>{@code INVOKE} - before or after a matching method invocation.
     *       {@code invokedynamic} instructions are not matched.</li>
     *   <li>{@code FIELD} - before or after a matching field access.</li>
     *   <li>{@code CONSTANT} - before or after a matching {@code LDC}
     *       constant.</li>
     * </ul>
     * The value is a plain string and is therefore not validated at compile time;
     * an unknown name fails at transform time with a {@code MixinException}.
     *
     * @return the injection point kind, {@code "TAIL"} by default
     */
    String value() default "TAIL";

    /**
     * Signature of the member to match, only used by {@code INVOKE}, {@code FIELD}
     * and {@code CONSTANT} points.
     * <p>
     * {@code INVOKE} uses {@code owner + "." + name + descriptor}, for example
     * {@code "java/io/PrintStream.println(Ljava/lang/String;)V"}.
     * {@code FIELD} uses {@code owner + "." + name + ":" + descriptor}, for
     * example {@code "java/lang/System.out:Ljava/io/PrintStream;"}.
     * {@code CONSTANT} uses the textual constant value: strings are matched
     * literally, numbers through {@link String#valueOf(Object)}, and class
     * constants by descriptor, class name or internal name. Only {@code LDC}
     * instructions are matched, so small integer constants that the compiler
     * emits as {@code ICONST_*} / {@code BIPUSH} are not injection points.
     * <p>
     * {@code owner} is an ASM internal name (slash separated, not a dotted Java
     * name). The default empty string matches every member of the selected kind.
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
     * <p>
     * {@link Shift#AFTER} is supported by {@code INVOKE}, {@code FIELD} and
     * {@code CONSTANT}. {@code HEAD} and {@code TAIL} only support
     * {@link Shift#BEFORE}.
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
}
