package cn.xiaozhou233.juicemixin.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 把目标方法的第 N 个参数传给注入方法。
 *
 * <p>{@code value} 是参数序号，从 0 开始，<b>不含 {@code this}</b>。
 * 形参类型必须与目标参数类型完全一致，框架不做装箱或宽化转换。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Arg {

    /** 参数序号，从 0 开始，不含 {@code this}。 */
    int value();
}
