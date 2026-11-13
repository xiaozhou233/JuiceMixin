package cn.xiaozhou233.juicemixin.example;

import cn.xiaozhou233.juicemixin.annotations.At;
import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;

@Mixin(Constructed.class)
public class ConstructedMixin {
    @Inject(
            method = "<init>",
            desc = "()V",
            at = @At("HEAD")
    )
    private void injectIntoConstructor() {
        System.out.println("CTOR");
    }
}
