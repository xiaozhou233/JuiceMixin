package cn.xiaozhou233.juicemixin.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 把目标对象实例传给注入方法。
 *
 * <p>注意：注入方法里的 {@code this} 是 mixin 实例，不是目标对象；要访问目标对象请用本注解。
 * 目标方法是静态方法时不能使用。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface This {
}
