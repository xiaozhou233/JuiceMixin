package cn.xiaozhou233.juicemixin.contents;

import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.exception.MixinException;
import lombok.Getter;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A mixin handler method together with every {@link Inject} annotation declared
 * on it.
 * <p>
 * {@code @Inject} is repeatable, so one handler can be injected at several
 * points. Each annotation is kept separately because every one of them may have
 * a different target location.
 */
@Getter
public class MixinMethodContent {
    private final MethodNode mixinMethod;
    private final List<Inject> injects;

    public MixinMethodContent(MethodNode mixinMethod) {
        this.mixinMethod = Objects.requireNonNull(mixinMethod, "mixinMethod");
        this.injects = Collections.unmodifiableList(
                Inject.Helper.getAnnotations(mixinMethod)
        );

        if (injects.isEmpty()) {
            throw new MixinException(
                    "Method " + mixinMethod.name + mixinMethod.desc
                            + " is not annotated with @Inject"
            );
        }
    }
}
