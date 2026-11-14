package cn.xiaozhou233.juicemixin.tests;

import cn.xiaozhou233.juicemixin.annotations.At;
import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;

/**
 * One handler with two injection points; both copies of its body must be
 * inserted independently.
 */
@Mixin(Target.class)
public class TargetRepeatMixin {
    @Inject(method = "run", desc = "()V", at = @At("HEAD"))
    @Inject(method = "run", desc = "()V", at = @At("TAIL"))
    private void injectTwice() {
        System.out.println("REPEAT");
    }
}
