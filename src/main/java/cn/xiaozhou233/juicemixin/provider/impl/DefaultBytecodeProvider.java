package cn.xiaozhou233.juicemixin.provider.impl;

import cn.xiaozhou233.juicemixin.provider.BytecodeProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads class bytes from the class loader that loaded the class, falling back to
 * the system class loader for bootstrap classes.
 */
public class DefaultBytecodeProvider implements BytecodeProvider {

    @Override
    public byte[] get(Class<?> type) {
        String resourceName = type.getName().replace('.', '/') + ".class";
        ClassLoader classLoader = type.getClassLoader();

        try (InputStream inputStream = classLoader != null
                ? classLoader.getResourceAsStream(resourceName)
                : ClassLoader.getSystemResourceAsStream(resourceName)) {

            if (inputStream == null) {
                return null;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            return outputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
}
