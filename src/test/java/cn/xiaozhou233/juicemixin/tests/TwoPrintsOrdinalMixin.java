package cn.xiaozhou233.juicemixin.tests;

import cn.xiaozhou233.juicemixin.annotations.At;
import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;

@Mixin(TwoPrints.class)
public class TwoPrintsOrdinalMixin {
    @Inject(
            method = "run",
            desc = "()V",
            at = @At(
                    value = "INVOKE",
                    target = "java/io/PrintStream.println(Ljava/lang/String;)V",
                    ordinal = 1
            )
    )
    private void beforeSecondPrint() {
        System.out.println("BEFORE SECOND");
    }
}
