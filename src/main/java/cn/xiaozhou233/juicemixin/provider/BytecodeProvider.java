package cn.xiaozhou233.juicemixin.provider;

public interface BytecodeProvider {
    byte[] get(Class<?> clazz);
}
