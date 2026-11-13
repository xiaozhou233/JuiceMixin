package cn.xiaozhou233.juicemixin.internal.metadata;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.List;

/**
 * Low level helpers for reading ASM annotation nodes.
 */
public final class AnnotationValues {

    private AnnotationValues() {
    }

    public static boolean isAnnotation(AnnotationNode node, Class<?> annotationType) {
        return node != null && Type.getDescriptor(annotationType).equals(node.desc);
    }

    public static AnnotationNode getAnnotation(ClassNode node, Class<?> annotationType) {
        return find(node.visibleAnnotations, node.invisibleAnnotations, annotationType);
    }

    public static AnnotationNode getAnnotation(MethodNode node, Class<?> annotationType) {
        return find(node.visibleAnnotations, node.invisibleAnnotations, annotationType);
    }

    private static AnnotationNode find(
            List<AnnotationNode> visible,
            List<AnnotationNode> invisible,
            Class<?> annotationType
    ) {
        AnnotationNode annotation = find(visible, annotationType);
        return annotation != null ? annotation : find(invisible, annotationType);
    }

    private static AnnotationNode find(
            List<AnnotationNode> annotations,
            Class<?> annotationType
    ) {
        if (annotations == null) {
            return null;
        }

        for (AnnotationNode annotation : annotations) {
            if (isAnnotation(annotation, annotationType)) {
                return annotation;
            }
        }

        return null;
    }

    /**
     * Reads an annotation attribute. ASM stores attributes as a flat
     * name/value list.
     */
    @SuppressWarnings("unchecked")
    public static <T> T value(AnnotationNode node, String name) {
        if (node == null || node.values == null) {
            return null;
        }

        List<Object> values = node.values;
        for (int i = 0; i + 1 < values.size(); i += 2) {
            if (name.equals(values.get(i))) {
                return (T) values.get(i + 1);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<AnnotationNode> annotationList(AnnotationNode node, String name) {
        Object value = value(node, name);

        if (!(value instanceof List)) {
            return Collections.emptyList();
        }

        return (List<AnnotationNode>) value;
    }
}
