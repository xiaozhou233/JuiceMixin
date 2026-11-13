package cn.xiaozhou233.juicemixin.example;

import cn.xiaozhou233.juicemixin.annotations.At;
import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;

@Mixin(Target.class)
public class TargetHeadMixin {
    @Inject(
            method = "run",
            desc = "()V",
            at = @At("HEAD")
    )
    private void injectAtHead() {
        System.out.println("HEAD");
    }
}
