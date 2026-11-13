package cn.xiaozhou233.juicemixin.internal.bytecode;

import java.util.Objects;

/**
 * Name plus descriptor of a method.
 */
public final class MethodRef {

    private final String name;
    private final String desc;

    public MethodRef(String name, String desc) {
        this.name = Objects.requireNonNull(name, "name");
        this.desc = Objects.requireNonNull(desc, "desc");
    }

    public String name() {
        return name;
    }

    public String desc() {
        return desc;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MethodRef)) {
            return false;
        }
        MethodRef that = (MethodRef) other;
        return name.equals(that.name) && desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + desc.hashCode();
    }

    @Override
    public String toString() {
        return name + desc;
    }
}
