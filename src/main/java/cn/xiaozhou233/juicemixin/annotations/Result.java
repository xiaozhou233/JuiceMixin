package cn.xiaozhou233.juicemixin.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 把目标方法的返回值传给注入方法。
 *
 * <p>只在 {@link At.Kind#RETURN}、{@link At.Kind#TAIL} 上有意义；
 * 形参类型必须与目标方法的返回类型完全一致。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Result {
}
