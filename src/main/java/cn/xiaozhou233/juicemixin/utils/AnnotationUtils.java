package cn.xiaozhou233.juicemixin.utils;

import cn.xiaozhou233.juicemixin.annotations.Mixin;
import cn.xiaozhou233.juicemixin.data.annotations.MixinData;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class AnnotationUtils {
    public static boolean isAnnotation(AnnotationNode annotationNode, Class<?> annotationClass) {
        return Type.getDescriptor(annotationClass).equals(annotationNode.desc);
    }

    public static boolean hasAnnotation(ClassNode classNode, Class<?> annotationClass) {
        return Stream.of(classNode.visibleAnnotations, classNode.invisibleAnnotations)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .anyMatch(annotation -> isAnnotation(annotation, annotationClass));
    }

    public static boolean hasAnnotation(MethodNode methodNode, Class<?> annotationClass) {
        return Stream.of(methodNode.visibleAnnotations, methodNode.invisibleAnnotations)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .anyMatch(annotation -> isAnnotation(annotation, annotationClass));
    }

    public static <T> T getAnnotationValue(AnnotationNode annotationNode, String name) {
        if (annotationNode == null || annotationNode.values == null)
            return null;

        List<Object> values = annotationNode.values;
        for (int i = 0; i < values.size() - 1; i += 2) {
            if (Objects.equals(values.get(i), name)) {
                @SuppressWarnings("unchecked")
                T value = (T) values.get(i + 1);
                return value;
            }
        }

        return null;
    }


    public static AnnotationNode getAnnotation(ClassNode classNode, Class<?> annotationClass) {
        return Stream.of(classNode.visibleAnnotations, classNode.invisibleAnnotations)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(annotation -> isAnnotation(annotation, annotationClass))
                .findFirst()
                .orElse(null);
    }

    public static AnnotationNode getAnnotation(MethodNode methodNode, Class<?> annotationClass) {
        return Stream.of(methodNode.visibleAnnotations, methodNode.invisibleAnnotations)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(annotation -> isAnnotation(annotation, annotationClass))
                .findFirst()
                .orElse(null);
    }

    public static AnnotationNode getParameterAnnotation(
            MethodNode methodNode,
            int parameterIndex,
            Class<?> annotationClass
    ) {
        if (parameterIndex < 0 || parameterIndex >= methodNode.visibleParameterAnnotations.length) {
            return getInvisibleParameterAnnotation(methodNode, parameterIndex, annotationClass);
        }

        List<AnnotationNode>[] visible = methodNode.visibleParameterAnnotations;
        List<AnnotationNode>[] invisible = methodNode.invisibleParameterAnnotations;

        AnnotationNode annotation = findParameterAnnotation(visible, parameterIndex, annotationClass);
        return annotation != null
                ? annotation
                : findParameterAnnotation(invisible, parameterIndex, annotationClass);
    }

    private static AnnotationNode getInvisibleParameterAnnotation(
            MethodNode methodNode,
            int parameterIndex,
            Class<?> annotationClass
    ) {
        return findParameterAnnotation(
                methodNode.invisibleParameterAnnotations, parameterIndex, annotationClass
        );
    }

    private static AnnotationNode findParameterAnnotation(
            List<AnnotationNode>[] annotations,
            int parameterIndex,
            Class<?> annotationClass
    ) {
        if (annotations == null || parameterIndex < 0 || parameterIndex >= annotations.length
                || annotations[parameterIndex] == null) {
            return null;
        }

        return annotations[parameterIndex].stream()
                .filter(annotation -> isAnnotation(annotation, annotationClass))
                .findFirst()
                .orElse(null);
    }
}
