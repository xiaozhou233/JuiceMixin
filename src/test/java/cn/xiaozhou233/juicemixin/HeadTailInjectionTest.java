package cn.xiaozhou233.juicemixin;

import cn.xiaozhou233.juicemixin.tests.Constructed;
import cn.xiaozhou233.juicemixin.tests.ConstructedMixin;
import cn.xiaozhou233.juicemixin.tests.Greeter;
import cn.xiaozhou233.juicemixin.tests.GreeterMixin;
import cn.xiaozhou233.juicemixin.tests.Target;
import cn.xiaozhou233.juicemixin.tests.TargetHead2Mixin;
import cn.xiaozhou233.juicemixin.tests.TargetHeadMixin;
import cn.xiaozhou233.juicemixin.tests.TargetMixin;
import cn.xiaozhou233.juicemixin.tests.TargetRepeatMixin;
import cn.xiaozhou233.juicemixin.tests.TargetTryMixin;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.xiaozhou233.juicemixin.TransformTestSupport.capture;
import static cn.xiaozhou233.juicemixin.TransformTestSupport.invokeRun;
import static cn.xiaozhou233.juicemixin.TransformTestSupport.transform;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HeadTailInjectionTest {

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
    void headInjectionsRunInMixinRegistrationOrder() throws Exception {
        assertEquals(
                Arrays.asList("HEAD", "HEAD2", "Target!", "Hello from JuiceMixin!"),
                invokeRun(transform(
                        Target.class,
                        TargetHeadMixin.class,
                        TargetHead2Mixin.class,
                        TargetMixin.class
                ))
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
}
