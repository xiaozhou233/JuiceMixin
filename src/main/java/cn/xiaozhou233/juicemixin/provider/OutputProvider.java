package cn.xiaozhou233.juicemixin.provider;

/**
 * Receives transformed class files.
 * <p>
 * {@code name} is the internal name of the class, for example
 * {@code cn/xiaozhou233/juicemixin/example/Target}.
 */
public interface OutputProvider {
    void output(String name, byte[] bytes);
}
