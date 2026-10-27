package cn.xiaozhou233.juicemixin.test;

import cn.xiaozhou233.juicemixin.CallbackInfo;
import cn.xiaozhou233.juicemixin.annotations.Arg;
import cn.xiaozhou233.juicemixin.annotations.At;
import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;
import cn.xiaozhou233.juicemixin.annotations.Result;

@Mixin(TargetClass.class)
public class TargetClassMixin {

    private int calls;

    @Inject(method = "run", desc = "()V")
    public void onRunHead(CallbackInfo ci) {
        calls++;
        ci.cancel();
    }

    @Inject(method = "run", desc = "()V", at = @At(At.Kind.RETURN))
    public void onRunReturn() {
        calls--;
    }

    @Inject(method = "add", desc = "(II)I")
    @Inject(method = "add", desc = "(II)I", at = @At(value = At.Kind.INVOKE, target = "java/lang/Math.addExact(II)I"))
    public void onAddCall(@Arg(0) int a, @Arg(1) int b) {
        System.out.println(a + " + " + b);
    }

    @Inject(method = "add", desc = "(II)I", at = @At(At.Kind.RETURN))
    public void onAddReturn(@Arg(0) int a, @Arg(1) int b, @Result int result) {
        System.out.println(a + " + " + b + " = " + result);
    }

    @Inject(method = "name", desc = "()Ljava/lang/String;", at = @At(At.Kind.RETURN))
    public void onNameReturn(@Result String result) {
        System.out.println(result);
    }
}
