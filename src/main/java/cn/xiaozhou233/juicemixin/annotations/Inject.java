package cn.xiaozhou233.juicemixin.annotations;

import cn.xiaozhou233.juicemixin.utils.AnnotationUtils;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.*;

/**
 * Attaches a method declared in a mixin to a specific position in a target method.
 *
 * <p>An injector method must be a {@code public} <b>instance method</b> with a
 * {@code void} return type. Its only permitted parameter type is
 * {@link cn.xiaozhou233.juicemixin.CallbackInfo}; if present, it must be the last
 * parameter.
 *
 * <p>Multiple {@code @Inject} annotations may be stacked on the same method
 * (see {@link Repeatable}); in that case the parameter list must be valid for
 * every injection point.
 *
 * <p>A failed match (missing target method, descriptor mismatch, injection point
 * not found) throws an exception and is never skipped silently.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Inject.List.class)
public @interface Inject {

    /** Name of the target method; use {@code "<init>"} for constructors. */
    String method();

    /** Exact JVM descriptor of the target method, e.g. {@code "(ILjava/lang/String;)V"}. */
    String desc();

    /** Injection point; defaults to {@code "HEAD"}. */
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
