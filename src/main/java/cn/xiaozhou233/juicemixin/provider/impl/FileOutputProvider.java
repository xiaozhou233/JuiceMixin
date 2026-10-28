package cn.xiaozhou233.juicemixin.provider.impl;

import cn.xiaozhou233.juicemixin.provider.OutputProvider;
import cn.xiaozhou233.juicemixin.utils.IOUtils;
import lombok.Setter;

import java.io.File;
import java.io.IOException;

@Setter
public class FileOutputProvider implements OutputProvider {

    private File outputDirectory = new File("bytes");

    @Override
    public void output(String name, byte[] bytes) {
        String path = name.replace('.', File.separatorChar)
                .replace('/', File.separatorChar) + ".class";

        File file = new File(outputDirectory, path);

        try {
            IOUtils.writeBytes(file, bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to output class: " + name, e);
        }
    }

}
