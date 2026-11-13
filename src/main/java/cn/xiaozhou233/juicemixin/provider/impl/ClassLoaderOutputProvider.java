package cn.xiaozhou233.juicemixin.provider.impl;

import cn.xiaozhou233.juicemixin.provider.OutputProvider;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * Defines transformed classes in a {@link MixinClassLoader}.
 */
public class ClassLoaderOutputProvider implements OutputProvider {

    @Getter
    @Setter
    private MixinClassLoader classLoader;

    public ClassLoaderOutputProvider(@Nullable ClassLoader parent) {
        this.classLoader = new MixinClassLoader(
                parent == null ? ClassLoader.getSystemClassLoader() : parent
        );
    }

    @Override
    public void output(String name, byte[] bytes) {
        classLoader.defineClass(name, bytes);
    }
}
