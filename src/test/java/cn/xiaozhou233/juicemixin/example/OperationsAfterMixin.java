package cn.xiaozhou233.juicemixin.example;

import cn.xiaozhou233.juicemixin.annotations.At;
import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;

@Mixin(Operations.class)
public class OperationsAfterMixin {
    @Inject(
            method = "run",
            desc = "()V",
            at = @At(
                    value = "INVOKE",
                    target = "java/io/PrintStream.println(Ljava/lang/String;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onInvokeAfter() {
        System.out.println("after");
    }
}
