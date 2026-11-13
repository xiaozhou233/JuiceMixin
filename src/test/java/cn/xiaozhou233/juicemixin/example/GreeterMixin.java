package cn.xiaozhou233.juicemixin.example;

import cn.xiaozhou233.juicemixin.annotations.At;
import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;

/**
 * Declares a local variable that would use the same slot as the {@code name}
 * parameter of the target method if locals were not moved.
 */
@Mixin(Greeter.class)
public class GreeterMixin {
    @Inject(
            method = "greet",
            desc = "(Ljava/lang/String;)Ljava/lang/String;",
            at = @At("HEAD")
    )
    private void onGreet() {
        String local = "SHADOW";
        System.out.println("mixin:" + local);
    }
}
