package cn.xiaozhou233.juicemixin;

import cn.xiaozhou233.juicemixin.example.Operations;
import cn.xiaozhou233.juicemixin.example.OperationsAfterMixin;
import cn.xiaozhou233.juicemixin.example.OperationsMixin;
import cn.xiaozhou233.juicemixin.example.TwoPrints;
import cn.xiaozhou233.juicemixin.example.TwoPrintsOrdinalMixin;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static cn.xiaozhou233.juicemixin.TransformTestSupport.invokeRun;
import static cn.xiaozhou233.juicemixin.TransformTestSupport.transform;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberInjectionTest {

    @Test
    void memberInjectionPointsRunAtMatchedInstructions() throws Exception {
        assertEquals(
                Arrays.asList("field", "constant", "invoke", "VALUE=7"),
                invokeRun(transform(Operations.class, OperationsMixin.class))
        );
    }

    @Test
    void afterShiftRunsAfterTheMatchedInstruction() throws Exception {
        assertEquals(
                Arrays.asList("VALUE=7", "after"),
                invokeRun(transform(Operations.class, OperationsAfterMixin.class))
        );
    }

    @Test
    void ordinalSelectsASingleMatch() throws Exception {
        assertEquals(
                Arrays.asList("ONE", "BEFORE SECOND", "TWO"),
                invokeRun(transform(TwoPrints.class, TwoPrintsOrdinalMixin.class))
        );
    }
}
