package cn.xiaozhou233.juicemixin.internal.transform;

import cn.xiaozhou233.juicemixin.MixinConfig;
import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.internal.bytecode.MethodBodyCloner;
import cn.xiaozhou233.juicemixin.internal.bytecode.MethodLookup;
import cn.xiaozhou233.juicemixin.internal.bytecode.PreparedBody;
import cn.xiaozhou233.juicemixin.internal.bytecode.TargetContext;
import cn.xiaozhou233.juicemixin.internal.injection.InjectionPoint;
import cn.xiaozhou233.juicemixin.internal.injection.InjectionPointFactory;
import cn.xiaozhou233.juicemixin.internal.injection.Insertion;
import cn.xiaozhou233.juicemixin.internal.injection.MethodInjector;
import cn.xiaozhou233.juicemixin.internal.metadata.HandlerDefinition;
import cn.xiaozhou233.juicemixin.internal.metadata.InjectionSpec;
import cn.xiaozhou233.juicemixin.internal.metadata.MixinDefinition;
import cn.xiaozhou233.juicemixin.provider.LoggerProvider;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Objects;

/**
 * Applies all handlers of one mixin to a target class.
 */
public final class MixinApplicator {

    private final MixinConfig config;
    private final MethodInjector injector = new MethodInjector();

    public MixinApplicator(MixinConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public void apply(TargetContext context, MixinDefinition mixin) {
        LoggerProvider logger = config.loggerProvider();

        logger.info(
                "Processing mixin {} for target {}",
                mixin.classNode().name,
                mixin.targetInternalName()
        );

        for (HandlerDefinition handler : mixin.handlers()) {
            for (InjectionSpec spec : handler.injections()) {
                applyInjection(context, mixin, handler, spec, logger);
            }
        }
    }

    private void applyInjection(
            TargetContext context,
            MixinDefinition mixin,
            HandlerDefinition handler,
            InjectionSpec spec,
            LoggerProvider logger
    ) {
        MethodNode targetMethod = MethodLookup.find(
                context.targetNode(), spec.targetMethod()
        );

        if (targetMethod == null) {
            throw new MixinException(
                    "Target method " + spec.targetMethod() + " not found in "
                            + mixin.targetInternalName() + " (required by mixin method "
                            + handler.method().name + handler.method().desc + ")"
            );
        }

        CompatibilityValidator.validate(mixin, handler, targetMethod, spec);

        InjectionPoint point = InjectionPointFactory.get(spec.kind());
        List<Insertion> insertions = point.find(context, targetMethod, spec);

        PreparedBody body = MethodBodyCloner.clone(handler.method());
        injector.inject(context, targetMethod, spec, body, insertions);

        logger.debug(
                "Injected {} into {}{} at {}",
                handler.method().name,
                targetMethod.name,
                targetMethod.desc,
                spec.kind()
        );
    }
}
