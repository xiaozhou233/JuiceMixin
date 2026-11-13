package cn.xiaozhou233.juicemixin.provider.impl;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A child-first class loader for classes produced by the transformer.
 * <p>
 * Classes defined here take precedence over the parent class loader, so an
 * already loaded original class does not shadow the transformed one.
 */
public class MixinClassLoader extends ClassLoader {

    private final Map<String, Class<?>> definedClasses = new ConcurrentHashMap<>();

    public MixinClassLoader(@Nullable ClassLoader parent) {
        super(parent);
    }

    /**
     * Defines a class from bytecode.
     *
     * @param name  internal name ({@code a/b/C}) or binary name ({@code a.b.C})
     * @param bytes class bytes
     * @return the defined class
     */
    public Class<?> defineClass(String name, byte[] bytes) {
        String binaryName = name.replace('/', '.');
        Class<?> defined = defineClass(binaryName, bytes, 0, bytes.length);
        definedClasses.put(binaryName, defined);
        return defined;
    }

    public Class<?> getDefinedClass(String name) {
        return definedClasses.get(name.replace('/', '.'));
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> defined = definedClasses.get(name);
        if (defined != null) {
            if (resolve) {
                resolveClass(defined);
            }
            return defined;
        }

        return super.loadClass(name, resolve);
    }
}
