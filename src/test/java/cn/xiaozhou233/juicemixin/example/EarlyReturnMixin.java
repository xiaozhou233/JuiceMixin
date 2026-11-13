package cn.xiaozhou233.juicemixin.example;

import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;

/**
 * Invalid on purpose: a void handler with an early return cannot be merged into
 * a method that returns a value.
 */
@Mixin(Greeter.class)
public class EarlyReturnMixin {
    @Inject(method = "greet", desc = "(Ljava/lang/String;)Ljava/lang/String;")
    private void onGreet() {
        if (System.nanoTime() == 0L) {
            return;
        }
        System.out.println("unreachable");
    }
}
