package cn.xiaozhou233.juicemixin.provider.impl;

import cn.xiaozhou233.juicemixin.provider.OutputProvider;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Defines transformed classes in a child class loader.
 * <p>
 * Classes that were defined here take precedence over the parent class loader,
 * so an already loaded original class does not shadow the transformed one.
 */
public class ClassLoaderOutputProvider implements OutputProvider {

    @Getter
    @Setter
    private CustomClassloader classLoader;

    public ClassLoaderOutputProvider(@Nullable ClassLoader classLoader) {
        this.classLoader = new CustomClassloader(
                classLoader == null
                        ? ClassLoader.getSystemClassLoader()
                        : classLoader
        );
    }

    @Override
    public void output(String name, byte[] bytes) {
        classLoader.defineClass(name, bytes);
    }

    public static class CustomClassloader extends ClassLoader {

        private final Map<String, Class<?>> definedClasses = new ConcurrentHashMap<>();

        public CustomClassloader(@Nullable ClassLoader parent) {
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
            Class<?> defined = super.defineClass(binaryName, bytes, 0, bytes.length);
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
}
