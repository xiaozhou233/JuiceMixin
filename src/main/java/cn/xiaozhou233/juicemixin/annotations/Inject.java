package cn.xiaozhou233.juicemixin.annotations;

import cn.xiaozhou233.juicemixin.utils.AnnotationUtils;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.*;

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

    class Helper {

        public static java.util.List<Inject> getAnnotations(MethodNode node) {
            AnnotationNode annotation = AnnotationUtils.getAnnotation(node, Inject.class);

            if (annotation != null) {
                return java.util.Collections.singletonList(getAnnotation(annotation));
            }

            AnnotationNode container = AnnotationUtils.getAnnotation(node, List.class);
            if (container == null) {
                return java.util.Collections.emptyList();
            }

            java.util.List<AnnotationNode> annotations =
                    AnnotationUtils.getAnnotationValue(container, "value");

            if (annotations == null) {
                return java.util.Collections.emptyList();
            }

            return annotations.stream()
                    .map(Helper::getAnnotation)
                    .collect(java.util.stream.Collectors.toList());
        }

        public static Inject getAnnotation(AnnotationNode node) {
            if (!AnnotationUtils.isAnnotation(node, Inject.class)) {
                return null;
            }

            String method = AnnotationUtils.getAnnotationValue(node, "method");
            String desc = AnnotationUtils.getAnnotationValue(node, "desc");

            AnnotationNode atNode = AnnotationUtils.getAnnotationValue(node, "at");
            At at = atNode == null
                    ? At.Helper.getDefaultAnnotation()
                    : At.Helper.getAnnotation(atNode);

            return new Inject() {
                @Override
                public String method() {
                    return method;
                }

                @Override
                public String desc() {
                    return desc;
                }

                @Override
                public At at() {
                    return at;
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return Inject.class;
                }
            };
        }
    }
}
