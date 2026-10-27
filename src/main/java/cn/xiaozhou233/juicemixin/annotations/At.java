package cn.xiaozhou233.juicemixin.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入点描述，只能作为 {@link Inject#at()} 的值使用，不能标在任何声明上。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface At {

    /** 注入点类型，缺省为 {@link Kind#HEAD}。 */
    Kind value() default Kind.HEAD;

    /**
     * 目标成员签名，只有 {@link Kind#INVOKE}、{@link Kind#FIELD}、{@link Kind#CONSTANT} 会用到。
     * 格式为 {@code "类名.成员名描述符"}，例如
     * {@code "java/io/PrintStream.println(Ljava/lang/String;)V"}。
     */
    String target() default "";

    /**
     * 第几个匹配项。{@code -1} 表示全部匹配，{@code >= 0} 表示只取第 N 个（从 0 开始）。
     */
    int ordinal() default -1;

    /**
     * 插在匹配指令之前还是之后，只有
     * {@link Kind#INVOKE}、{@link Kind#FIELD}、{@link Kind#CONSTANT} 会用到。
     */
    Shift shift() default Shift.BEFORE;

    enum Kind {
        /** 方法体第一条指令之前。 */
        HEAD,
        /** 每一条 return 指令之前。 */
        RETURN,
        /** 方法体最后一条 return 指令之前。 */
        TAIL,
        /** 匹配到的方法调用处。 */
        INVOKE,
        /** 匹配到的字段访问处。 */
        FIELD,
        /** 匹配到的常量压栈处。 */
        CONSTANT
    }

    enum Shift {
        BEFORE,
        AFTER
    }
}
