package cn.xiaozhou233.juicemixin.provider.impl;

import cn.xiaozhou233.juicemixin.provider.ClassProvider;

/**
 * Resolves classes through the context class loader, falling back to the class
 * loader of the library.
 */
public class DefaultClassProvider implements ClassProvider {

    @Override
    public Class<?> get(String internalName) {
        String className = internalName.replace('/', '.');
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        Class<?> resolved = load(className, contextLoader);

        if (resolved == null) {
            resolved = load(className, DefaultClassProvider.class.getClassLoader());
        }

        return resolved;
    }

    private static Class<?> load(String className, ClassLoader classLoader) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
