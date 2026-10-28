package cn.xiaozhou233.juicemixin.annotations;

import cn.xiaozhou233.juicemixin.utils.AnnotationUtils;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.*;

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

    class Helper {

        public static At getAnnotation(AnnotationNode node) {
            if (!AnnotationUtils.isAnnotation(node, At.class)) {
                return null;
            }

            Kind kind = getEnumValue(node, "value", Kind.class, Kind.HEAD);
            String target = getValue(node, "target", "");
            int ordinal = getValue(node, "ordinal", -1);
            Shift shift = getEnumValue(node, "shift", Shift.class, Shift.BEFORE);

            return new At() {
                @Override
                public Kind value() {
                    return kind;
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
                public Kind value() {
                    return Kind.HEAD;
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
