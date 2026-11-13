package cn.xiaozhou233.juicemixin;

import cn.xiaozhou233.juicemixin.provider.impl.MixinClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class TransformTestSupport {

    private TransformTestSupport() {
    }

    static Class<?> transform(Class<?> targetClass, Class<?>... mixins) throws Exception {
        Map<String, byte[]> transformed = new MixinTransformer(MixinConfig.defaults())
                .transform(mixins);
        assertTrue(!transformed.isEmpty(), "nothing was transformed");

        MixinClassLoader loader = new MixinClassLoader(TransformTestSupport.class.getClassLoader());
        for (Map.Entry<String, byte[]> entry : transformed.entrySet()) {
            assertValidBytecode(entry.getValue(), loader);
            loader.defineClass(entry.getKey(), entry.getValue());
        }

        return loader.getDefinedClass(targetClass.getName());
    }

    static List<String> invokeRun(Class<?> targetClass) throws Exception {
        return capture(() -> {
            Object instance = targetClass.getDeclaredConstructor().newInstance();
            targetClass.getMethod("run").invoke(instance);
        });
    }

    static List<String> capture(Action action) throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(buffer, true, "UTF-8");

        System.setOut(capture);
        try {
            action.run();
        } finally {
            System.setOut(originalOut);
            capture.flush();
        }

        String output = new String(buffer.toByteArray(), "UTF-8").trim();
        if (output.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> lines = new ArrayList<>();
        for (String line : output.split("\\r?\\n")) {
            lines.add(line);
        }
        return lines;
    }

    private static void assertValidBytecode(byte[] bytes, ClassLoader loader) {
        StringWriter diagnostics = new StringWriter();
        CheckClassAdapter.verify(
                new ClassReader(bytes),
                loader,
                false,
                new PrintWriter(diagnostics)
        );
        assertTrue(
                diagnostics.toString().isEmpty(),
                "invalid bytecode:\n" + diagnostics
        );
    }

    interface Action {
        void run() throws Exception;
    }
}
