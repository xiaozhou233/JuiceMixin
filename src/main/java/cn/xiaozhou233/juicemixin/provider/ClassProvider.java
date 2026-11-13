package cn.xiaozhou233.juicemixin.provider;

/**
 * Resolves loaded classes by their ASM internal name.
 * <p>
 * Used to resolve mixin targets and to compute common super classes while stack
 * map frames are recalculated.
 */
public interface ClassProvider {

    /**
     * Resolves an internal name such as {@code java/lang/String}.
     *
     * @param internalName slash separated class name
     * @return the class, or {@code null} if it cannot be resolved
     */
    Class<?> get(String internalName);
}
