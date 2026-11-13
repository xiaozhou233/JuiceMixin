package cn.xiaozhou233.juicemixin.internal.injection;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Objects;

/**
 * One concrete position where a body has to be inserted.
 */
public final class Insertion {

    private final AbstractInsnNode anchor;
    private final boolean after;

    private Insertion(AbstractInsnNode anchor, boolean after) {
        this.anchor = Objects.requireNonNull(anchor, "anchor");
        this.after = after;
    }

    public static Insertion before(AbstractInsnNode anchor) {
        return new Insertion(anchor, false);
    }

    public static Insertion after(AbstractInsnNode anchor) {
        return new Insertion(anchor, true);
    }

    public AbstractInsnNode anchor() {
        return anchor;
    }

    public boolean isAfter() {
        return after;
    }
}
