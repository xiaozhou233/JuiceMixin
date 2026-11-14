package cn.xiaozhou233.juicemixin;

import cn.xiaozhou233.juicemixin.tests.EarlyReturnMixin;
import cn.xiaozhou233.juicemixin.tests.MissingInvokeMixin;
import cn.xiaozhou233.juicemixin.exception.MixinException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationTest {

    @Test
    void earlyReturnCannotBeInjectedIntoNonVoidTarget() {
        assertThrows(
                MixinException.class,
                () -> new MixinTransformer(MixinConfig.defaults())
                        .transform(EarlyReturnMixin.class)
        );
    }

    @Test
    void missingMemberMatchFails() {
        assertThrows(
                MixinException.class,
                () -> new MixinTransformer(MixinConfig.defaults())
                        .transform(MissingInvokeMixin.class)
        );
    }
}
