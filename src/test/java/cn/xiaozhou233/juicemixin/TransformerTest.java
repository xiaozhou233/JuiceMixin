package cn.xiaozhou233.juicemixin;

import cn.xiaozhou233.juicemixin.example.Constructed;
import cn.xiaozhou233.juicemixin.example.ConstructedMixin;
import cn.xiaozhou233.juicemixin.example.EarlyReturnMixin;
import cn.xiaozhou233.juicemixin.example.Greeter;
import cn.xiaozhou233.juicemixin.example.GreeterMixin;
import cn.xiaozhou233.juicemixin.example.MissingInvokeMixin;
import cn.xiaozhou233.juicemixin.example.Operations;
import cn.xiaozhou233.juicemixin.example.OperationsAfterMixin;
import cn.xiaozhou233.juicemixin.example.OperationsMixin;
import cn.xiaozhou233.juicemixin.example.Target;
import cn.xiaozhou233.juicemixin.example.TargetHeadMixin;
import cn.xiaozhou233.juicemixin.example.TargetMixin;
import cn.xiaozhou233.juicemixin.example.TargetRepeatMixin;
import cn.xiaozhou233.juicemixin.example.TargetTryMixin;
import cn.xiaozhou233.juicemixin.example.TwoPrints;
import cn.xiaozhou233.juicemixin.example.TwoPrintsOrdinalMixin;
import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.provider.impl.ClassLoaderOutputProvider;
import cn.xiaozhou233.juicemixin.provider.impl.EmptyOutputProvider;
import cn.xiaozhou233.juicemixin.registries.MixinRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransformerTest {

    @BeforeEach
    void resetProviders() {
        JuiceMixin.init();
        JuiceMixin.setOutputProvider(new EmptyOutputProvider());
    }

    @Test
    void tailInjectionRunsAfterOriginalBody() throws Exception {
        assertEquals(
                Arrays.asList("Target!", "Hello from JuiceMixin!"),
                invokeRun(transform(Target.class, TargetMixin.class))
        );
    }

    @Test
    void multipleMixinsAreAppliedToTheSameTarget() throws Exception {
        assertEquals(
                Arrays.asList("HEAD", "Target!", "Hello from JuiceMixin!"),
                invokeRun(transform(Target.class, TargetMixin.class, TargetHeadMixin.class))
        );
    }

    @Test
    void repeatableInjectUsesAFreshBodyForEveryPoint() throws Exception {
        assertEquals(
                Arrays.asList("REPEAT", "Target!", "REPEAT"),
                invokeRun(transform(Target.class, TargetRepeatMixin.class))
        );
    }

    @Test
    void tryCatchInHandlerKeepsLabelsAndHandlers() throws Exception {
        assertEquals(
                Arrays.asList("Target!", "TRY"),
                invokeRun(transform(Target.class, TargetTryMixin.class))
        );
    }

    @Test
    void headInjectionInConstructorRunsAfterSuperCall() throws Exception {
        Class<?> constructed = transform(Constructed.class, ConstructedMixin.class);

        List<String> output = capture(() -> {
            Object instance = constructed.getDeclaredConstructor().newInstance();
            assertEquals("original", constructed.getField("value").get(instance));
        });

        assertEquals(Collections.singletonList("CTOR"), output);
    }

    @Test
    void handlerLocalsDoNotOverwriteTargetArguments() throws Exception {
        Class<?> greeter = transform(Greeter.class, GreeterMixin.class);
        String[] greeting = new String[1];

        List<String> output = capture(() -> {
            Object instance = greeter.getDeclaredConstructor().newInstance();
            greeting[0] = (String) greeter
                    .getMethod("greet", String.class)
                    .invoke(instance, "World");
        });

        assertEquals(Collections.singletonList("mixin:SHADOW"), output);
        assertEquals("Hello, World", greeting[0]);
    }

    @Test
    void earlyReturnCannotBeInjectedIntoNonVoidTarget() {
        MixinRegistry registry = new MixinRegistry();
        registry.addMixin(EarlyReturnMixin.class);

        assertThrows(MixinException.class, () -> new Transformer(registry).transform());
    }

    @Test
    void memberInjectionPointsRunAtMatchedInstructions() throws Exception {
        assertEquals(
                Arrays.asList("field", "constant", "invoke", "VALUE=7"),
                invokeRun(transform(Operations.class, OperationsMixin.class))
        );
    }

    @Test
    void afterShiftRunsAfterTheMatchedInstruction() throws Exception {
        assertEquals(
                Arrays.asList("VALUE=7", "after"),
                invokeRun(transform(Operations.class, OperationsAfterMixin.class))
        );
    }

    @Test
    void ordinalSelectsASingleMatch() throws Exception {
        assertEquals(
                Arrays.asList("ONE", "BEFORE SECOND", "TWO"),
                invokeRun(transform(TwoPrints.class, TwoPrintsOrdinalMixin.class))
        );
    }

    @Test
    void missingMemberMatchFails() {
        MixinRegistry registry = new MixinRegistry();
        registry.addMixin(MissingInvokeMixin.class);

        assertThrows(MixinException.class, () -> new Transformer(registry).transform());
    }

    private static Class<?> transform(Class<?> targetClass, Class<?>... mixins) throws Exception {
        MixinRegistry registry = new MixinRegistry();
        registry.addMixin(mixins);

        Map<String, byte[]> transformed = new Transformer(registry).transform();
        assertTrue(!transformed.isEmpty(), "nothing was transformed");

        ClassLoaderOutputProvider.CustomClassloader loader =
                new ClassLoaderOutputProvider.CustomClassloader(TransformerTest.class.getClassLoader());

        for (Map.Entry<String, byte[]> entry : transformed.entrySet()) {
            assertValidBytecode(entry.getValue(), loader);
            loader.defineClass(entry.getKey(), entry.getValue());
        }

        return loader.getDefinedClass(targetClass.getName());
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

    private static List<String> invokeRun(Class<?> targetClass) throws Exception {
        return capture(() -> {
            Object instance = targetClass.getDeclaredConstructor().newInstance();
            targetClass.getMethod("run").invoke(instance);
        });
    }

    private static List<String> capture(Action action) throws Exception {
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

    private interface Action {
        void run() throws Exception;
    }
}
