package cn.xiaozhou233.juicemixin.example;

import cn.xiaozhou233.juicemixin.annotations.At;
import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;

/**
 * Invalid on purpose: the target signature never occurs in {@link TwoPrints}.
 */
@Mixin(TwoPrints.class)
public class MissingInvokeMixin {
    @Inject(
            method = "run",
            desc = "()V",
            at = @At(value = "INVOKE", target = "java/io/PrintStream.missing()V")
    )
    private void onMissingInvoke() {
    }
}
