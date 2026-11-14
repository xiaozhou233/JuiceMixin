package cn.xiaozhou233.juicemixin.tests;

import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;

/**
 * Handler with a try-catch block: labels and handler ranges have to be cloned
 * together with the instructions.
 */
@Mixin(Target.class)
public class TargetTryMixin {
    @Inject(method = "run", desc = "()V")
    private void injectWithTryCatch() {
        try {
            System.out.println("TRY");
        } catch (RuntimeException e) {
            System.out.println("CATCH");
        }
    }
}
