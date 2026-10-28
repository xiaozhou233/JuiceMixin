package cn.xiaozhou233.juicemixin.provider.impl;

import cn.xiaozhou233.juicemixin.provider.OutputProvider;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

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

        public CustomClassloader(@Nullable ClassLoader parent) {
            super(parent);
        }

        public Class<?> defineClass(String name, byte[] bytes) {
            return super.defineClass(name, bytes, 0, bytes.length);
        }
    }
}