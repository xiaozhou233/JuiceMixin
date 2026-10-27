package cn.xiaozhou233.juicemixin.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明当前类是目标类的 mixin。
 *
 * <p>被标记的类必须满足：
 * <ul>
 *   <li>是 {@code public}、非 {@code abstract} 的普通类；</li>
 *   <li>提供无参构造器。框架会为<b>每一个目标实例</b>创建一个 mixin 实例并缓存起来。</li>
 * </ul>
 *
 * <p>因此 mixin 的成员字段是跟着目标实例走的状态，不同目标实例之间互不影响。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mixin {

    /**
     * 目标类，可以有多个。同一个 mixin 里的注入方法必须对所有目标都成立。
     */
    Class<?>[] value();
}
