package cn.xiaozhou233.juicemixin.tests;

import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;

@Mixin(Target.class)
public class TargetMixin {
    @Inject(
            method = "run",
            desc = "()V"
    )
    private void injectRun() {
        System.out.println("Hello from JuiceMixin!");
    }
}
