package cn.xiaozhou233.juicemixin.test;

import cn.xiaozhou233.juicemixin.CallbackInfo;
import cn.xiaozhou233.juicemixin.annotations.Arg;
import cn.xiaozhou233.juicemixin.annotations.At;
import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;
import cn.xiaozhou233.juicemixin.annotations.Result;
import cn.xiaozhou233.juicemixin.annotations.This;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiContractTest {

    @Test
    void mixinDeclaresTargetClass() {
        Mixin mixin = TargetClassMixin.class.getAnnotation(Mixin.class);
        assertNotNull(mixin);
        assertEquals(TargetClass.class, mixin.value());
    }

    @Test
    void injectIsRepeatable() throws NoSuchMethodException {
        Method method = TargetClassMixin.class.getMethod("onAddCall", int.class, int.class);
        Inject[] injects = method.getAnnotationsByType(Inject.class);

        assertEquals(2, injects.length);
        assertEquals(At.Kind.HEAD, injects[0].at().value());
        assertEquals(At.Kind.INVOKE, injects[1].at().value());
        assertEquals("java/lang/Math.addExact(II)I", injects[1].at().target());
    }

    @Test
    void atDefaultsAreApplied() throws NoSuchMethodException {
        Inject inject = TargetClassMixin.class
                .getMethod("onRunHead", CallbackInfo.class)
                .getAnnotation(Inject.class);

        assertNotNull(inject);
        assertEquals("run", inject.method());
        assertEquals("()V", inject.desc());

        At at = inject.at();
        assertEquals(At.Kind.HEAD, at.value());
        assertEquals("", at.target());
        assertEquals(-1, at.ordinal());
        assertEquals(At.Shift.BEFORE, at.shift());
    }

    @Test
    void parameterBindingsAreExplicit() throws NoSuchMethodException {
        Method method = TargetClassMixin.class.getMethod("onAddReturn", int.class, int.class, int.class);
        Annotation[][] parameters = method.getParameterAnnotations();

        assertInstanceOf(Arg.class, parameters[0][0]);
        assertEquals(0, ((Arg) parameters[0][0]).value());
        assertEquals(1, ((Arg) parameters[1][0]).value());
        assertInstanceOf(Result.class, parameters[2][0]);
    }

    @Test
    void thisBindsTargetInstanceOnDemand() throws NoSuchMethodException {
        Method method = ThisBindingFixture.class.getMethod("withTarget", TargetClass.class);

        assertInstanceOf(This.class, method.getParameterAnnotations()[0][0]);
    }

    @Test
    void staticTargetDoesNotNeedThisParameter() throws NoSuchMethodException {
        Method method = TargetClassMixin.class.getMethod("onNameReturn", String.class);

        assertInstanceOf(Result.class, method.getParameterAnnotations()[0][0]);
        assertEquals(1, method.getParameterCount());
    }

    @Test
    void atCannotBePlacedOnAnyDeclaration() {
        Target target = At.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertEquals(0, target.value().length);
    }

    @Test
    void allAnnotationsAreRuntimeRetained() {
        Class<?>[] annotations = {
                Mixin.class, Inject.class, At.class, This.class, Arg.class, Result.class
        };
        for (Class<?> annotation : annotations) {
            Retention retention = annotation.getAnnotation(Retention.class);
            assertNotNull(retention, annotation.getName());
            assertEquals(RetentionPolicy.RUNTIME, retention.value(), annotation.getName());
        }
    }

    @Test
    void parameterAnnotationsOnlyApplyToParameters() {
        Class<?>[] annotations = {This.class, Arg.class, Result.class};
        for (Class<?> annotation : annotations) {
            Target target = annotation.getAnnotation(Target.class);
            assertNotNull(target, annotation.getName());
            assertArrayEquals(new ElementType[]{ElementType.PARAMETER}, target.value(), annotation.getName());
        }
    }

    @Test
    void callbackInfoCancelsWithoutValue() {
        CallbackInfo callbackInfo = new CallbackInfo();

        assertFalse(callbackInfo.isCancelled());
        callbackInfo.cancel();

        assertTrue(callbackInfo.isCancelled());
        assertEquals(null, callbackInfo.getReturnValue());
    }

    @Test
    void callbackInfoCancelsWithValue() {
        CallbackInfo callbackInfo = new CallbackInfo();

        callbackInfo.cancel(42);

        assertTrue(callbackInfo.isCancelled());
        assertEquals(42, callbackInfo.getReturnValue());
    }

    /** 只用来验证 @This 的绑定方式，不参与示例。 */
    @Mixin(TargetClass.class)
    static class ThisBindingFixture {

        @Inject(method = "run", desc = "()V")
        public void withTarget(@This TargetClass self) {
        }
    }
}
