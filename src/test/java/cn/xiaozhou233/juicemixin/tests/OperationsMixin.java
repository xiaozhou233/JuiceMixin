package cn.xiaozhou233.juicemixin.tests;

import cn.xiaozhou233.juicemixin.annotations.At;
import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;

@Mixin(Operations.class)
public class OperationsMixin {
    @Inject(
            method = "run",
            desc = "()V",
            at = @At(
                    value = "FIELD",
                    target = "cn/xiaozhou233/juicemixin/tests/Operations.value:I"
            )
    )
    private void onFieldRead() {
        System.out.println("field");
    }

    @Inject(
            method = "run",
            desc = "()V",
            at = @At(value = "CONSTANT", target = "VALUE=")
    )
    private void onConstant() {
        System.out.println("constant");
    }

    @Inject(
            method = "run",
            desc = "()V",
            at = @At(
                    value = "INVOKE",
                    target = "java/io/PrintStream.println(Ljava/lang/String;)V"
            )
    )
    private void onInvoke() {
        System.out.println("invoke");
    }
}
