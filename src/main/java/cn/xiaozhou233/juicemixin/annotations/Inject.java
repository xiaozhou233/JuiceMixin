package cn.xiaozhou233.juicemixin.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 把 mixin 里的一个方法挂到目标方法的指定位置上。
 *
 * <p>注入方法必须是 {@code public} 的<b>实例方法</b>，返回类型为 {@code void}。
 * 形参只能来自 {@link This}、{@link Arg}、{@link Result} 以及
 * {@link cn.xiaozhou233.juicemixin.CallbackInfo}，顺序不限；
 * {@code CallbackInfo} 如果出现，必须是最后一个参数。
 *
 * <p>同一个方法上可以叠加多个 {@code @Inject}（见 {@link Repeatable}），
 * 此时这份形参列表必须对所有注入点都合法。
 *
 * <p>匹配失败（目标方法不存在、描述符不符、注入点找不到）会直接抛异常，不会静默跳过。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Inject.List.class)
public @interface Inject {

    /** 目标方法名；构造器写 {@code "<init>"}。 */
    String method();

    /** 目标方法的 JVM 描述符，精确匹配，例如 {@code "(ILjava/lang/String;)V"}。 */
    String desc();

    /** 注入点，缺省为 {@link At.Kind#HEAD}。 */
    At at() default @At;

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface List {
        Inject[] value();
    }
}
