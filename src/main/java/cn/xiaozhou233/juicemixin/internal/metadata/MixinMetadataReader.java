package cn.xiaozhou233.juicemixin.internal.metadata;

import cn.xiaozhou233.juicemixin.MixinConfig;
import cn.xiaozhou233.juicemixin.annotations.At;
import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;
import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.internal.bytecode.ClassNodes;
import cn.xiaozhou233.juicemixin.internal.bytecode.MethodRef;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Reads mixin metadata from bytecode.
 */
public final class MixinMetadataReader {

    private final MixinConfig config;

    public MixinMetadataReader(MixinConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public List<MixinDefinition> read(Collection<Class<?>> mixins) {
        List<MixinDefinition> definitions = new ArrayList<>();

        for (Class<?> mixin : mixins) {
            definitions.add(read(mixin));
        }

        return definitions;
    }

    private MixinDefinition read(Class<?> mixinClass) {
        byte[] bytes = config.bytecodeProvider().get(mixinClass);
        if (bytes == null || bytes.length == 0) {
            throw new MixinException(
                    "Cannot read bytecode of mixin " + mixinClass.getName()
            );
        }

        ClassNode classNode = ClassNodes.read(bytes);
        AnnotationNode mixinAnnotation = AnnotationValues.getAnnotation(
                classNode, Mixin.class
        );

        if (mixinAnnotation == null) {
            throw new MixinException(
                    "Class " + mixinClass.getName() + " is not annotated with @Mixin"
            );
        }

        Type targetType = AnnotationValues.value(mixinAnnotation, "value");
        if (targetType == null) {
            throw new MixinException(
                    "@Mixin on " + mixinClass.getName() + " has no target class"
            );
        }

        String targetInternalName = targetType.getInternalName();
        Class<?> targetClass = config.classProvider().get(targetInternalName);
        if (targetClass == null) {
            throw new MixinException(
                    "Cannot resolve target class " + targetType.getClassName()
                            + " of mixin " + mixinClass.getName()
            );
        }

        List<HandlerDefinition> handlers = new ArrayList<>();
        for (MethodNode method : classNode.methods) {
            List<InjectionSpec> injections = readInjections(mixinClass, method);
            if (!injections.isEmpty()) {
                handlers.add(new HandlerDefinition(method, injections));
            }
        }

        return new MixinDefinition(
                mixinClass,
                classNode,
                targetClass,
                targetInternalName,
                handlers
        );
    }

    private List<InjectionSpec> readInjections(Class<?> mixinClass, MethodNode method) {
        List<AnnotationNode> injections = new ArrayList<>();

        AnnotationNode single = AnnotationValues.getAnnotation(method, Inject.class);
        if (single != null) {
            injections.add(single);
        } else {
            AnnotationNode container = AnnotationValues.getAnnotation(
                    method, Inject.List.class
            );
            if (container != null) {
                injections.addAll(AnnotationValues.annotationList(container, "value"));
            }
        }

        if (injections.isEmpty()) {
            return Collections.emptyList();
        }

        List<InjectionSpec> specs = new ArrayList<>();
        for (AnnotationNode injection : injections) {
            specs.add(readInjection(mixinClass, method, injection));
        }

        return specs;
    }

    private InjectionSpec readInjection(
            Class<?> mixinClass,
            MethodNode handler,
            AnnotationNode injection
    ) {
        String targetMethodName = AnnotationValues.value(injection, "method");
        String targetMethodDesc = AnnotationValues.value(injection, "desc");

        if (targetMethodName == null || targetMethodName.isEmpty()) {
            throw new MixinException(
                    "@Inject on " + describe(mixinClass, handler) + " has no target method name"
            );
        }
        if (targetMethodDesc == null || targetMethodDesc.isEmpty()) {
            throw new MixinException(
                    "@Inject on " + describe(mixinClass, handler)
                            + " has no target method descriptor"
            );
        }

        AnnotationNode atNode = AnnotationValues.value(injection, "at");
        String atValue = valueOrDefault(atNode, "value", "TAIL");
        String target = valueOrDefault(atNode, "target", "");
        int ordinal = valueOrDefault(atNode, "ordinal", -1);
        At.Shift annotationShift = readShift(atNode);

        InjectionPointKind kind = InjectionPointKind.parse(atValue);
        InsertionShift shift = InsertionShift.valueOf(annotationShift.name());

        return new InjectionSpec(
                kind,
                new MethodRef(targetMethodName, targetMethodDesc),
                target,
                ordinal,
                shift
        );
    }

    private static At.Shift readShift(AnnotationNode atNode) {
        String[] value = AnnotationValues.value(atNode, "shift");
        if (value == null || value.length < 2) {
            return At.Shift.BEFORE;
        }

        try {
            return At.Shift.valueOf(value[1]);
        } catch (IllegalArgumentException e) {
            throw new MixinException("Unknown @At shift: " + value[1], e);
        }
    }

    private static <T> T valueOrDefault(AnnotationNode node, String name, T defaultValue) {
        T value = AnnotationValues.value(node, name);
        return value != null ? value : defaultValue;
    }

    private static String describe(Class<?> mixinClass, MethodNode method) {
        return mixinClass.getName() + "#" + method.name + method.desc;
    }
}
