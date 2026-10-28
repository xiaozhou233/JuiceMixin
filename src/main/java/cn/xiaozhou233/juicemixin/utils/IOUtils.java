package cn.xiaozhou233.juicemixin.utils;

import cn.xiaozhou233.juicemixin.JuiceMixin;

import java.io.*;

public class IOUtils {
    public static byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toByteArray();
    }

    public static void writeBytes(File file, byte[] bytes) throws IOException {
        File parent = file.getParentFile();

        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            JuiceMixin.getLoggerProvider().error("Failed to create directory: " + parent);
        }

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(bytes);
        }
    }
}
