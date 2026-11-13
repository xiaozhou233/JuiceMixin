package cn.xiaozhou233.juicemixin.provider;

/**
 * Supplies class bytecode to the transformer.
 */
public interface BytecodeProvider {

    /**
     * Reads the bytecode of a class.
     *
     * @param type class to read
     * @return the class bytes, or {@code null} if they are not available
     */
    byte[] get(Class<?> type);
}
