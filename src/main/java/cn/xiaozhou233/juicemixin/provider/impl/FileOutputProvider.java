package cn.xiaozhou233.juicemixin.provider.impl;

import cn.xiaozhou233.juicemixin.provider.OutputProvider;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Writes transformed classes into a directory, keeping the package structure.
 */
@Setter
public class FileOutputProvider implements OutputProvider {

    private File outputDirectory = new File("bytes");

    @Override
    public void output(String name, byte[] bytes) {
        String path = name.replace('.', File.separatorChar)
                .replace('/', File.separatorChar) + ".class";
        File file = new File(outputDirectory, path);
        File parent = file.getParentFile();

        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + parent);
        }

        try {
            Files.write(file.toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to output class: " + name, e);
        }
    }
}
