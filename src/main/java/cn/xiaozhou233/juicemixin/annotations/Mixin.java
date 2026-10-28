package cn.xiaozhou233.juicemixin.annotations;

import cn.xiaozhou233.juicemixin.utils.AnnotationUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.lang.annotation.*;

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
     * 目标类。
     */
    Class<?> value();


    class Helper {

        public static Mixin getAnnotation(ClassNode node) {
            AnnotationNode annotation = AnnotationUtils.getAnnotation(node, Mixin.class);
            if (annotation == null) {
                return null;
            }

            Type type = AnnotationUtils.getAnnotationValue(annotation, "value");
            if (type == null) {
                throw new IllegalStateException("Mixin annotation has no target class");
            }

            String name = type.getClassName();
            Class<?> target;
            try {
                target = Class.forName(
                        name,
                        false,
                        Thread.currentThread().getContextClassLoader()
                );
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "Cannot load mixin target: " + name, e
                );
            }

            return new Mixin() {
                @Override
                public Class<?> value() {
                    return target;
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return Mixin.class;
                }
            };
        }
    }

}
