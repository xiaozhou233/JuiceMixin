package cn.xiaozhou233.juicemixin.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a mixin and declares the class its code is mixed into.
 * <p>
 * Mixing happens at runtime, so the target class must be resolvable by the
 * configured class provider when the mixin is processed. The annotation therefore
 * cannot be used to declare a target that does not exist (yet) in the running
 * JVM.
 * <p>
 * Retention is {@link RetentionPolicy#RUNTIME} so the annotation stays readable
 * through reflection as well; the transformer reads the bytecode form.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mixin {

    /**
     * The class to mix into. It is read back from the bytecode as a type and then
     * resolved through the configured {@code ClassProvider}.
     *
     * @return the target class
     */
    Class<?> value();
}
