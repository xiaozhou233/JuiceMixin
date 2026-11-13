package cn.xiaozhou233.juicemixin.internal.injection;

import cn.xiaozhou233.juicemixin.internal.bytecode.LocalSlotAllocator;
import cn.xiaozhou233.juicemixin.internal.bytecode.PreparedBody;
import cn.xiaozhou233.juicemixin.internal.bytecode.TargetContext;
import cn.xiaozhou233.juicemixin.internal.metadata.InjectionPointKind;
import cn.xiaozhou233.juicemixin.internal.metadata.InjectionSpec;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Inserts a prepared body at the positions selected by an injection point.
 */
public final class MethodInjector {

    public void inject(
            TargetContext context,
            MethodNode targetMethod,
            InjectionSpec spec,
            PreparedBody body,
            List<Insertion> insertions
    ) {
        // Prepare every insertion before shifting any of them: copy() must see
        // the original local variable indices.
        List<PreparedBody> bodies = new ArrayList<>(insertions.size());
        bodies.add(body);
        for (int i = 1; i < insertions.size(); i++) {
            bodies.add(body.copy());
        }

        for (int i = 0; i < insertions.size(); i++) {
            PreparedBody insertion = bodies.get(i);
            allocateLocalSlots(context, targetMethod, insertion);

            // Capture the cursor before insertion: InsnList.insert() detaches the
            // list and resets its first/last pointers.
            AbstractInsnNode lastInstruction = insertion.lastInstruction();

            Insertion target = insertions.get(i);
            if (target.isAfter()) {
                targetMethod.instructions.insert(
                        target.anchor(), insertion.instructions()
                );
            } else {
                targetMethod.instructions.insertBefore(
                        target.anchor(), insertion.instructions()
                );
            }

            registerTryCatchBlocks(targetMethod, insertion);

            if (spec.kind() == InjectionPointKind.HEAD && lastInstruction != null) {
                context.headCursor(targetMethod, lastInstruction);
            }
        }
    }

    private static void allocateLocalSlots(
            TargetContext context,
            MethodNode targetMethod,
            PreparedBody body
    ) {
        int requiredSlots = body.localSlotCount();
        if (requiredSlots <= 0) {
            return;
        }

        LocalSlotAllocator allocator = context.allocatorFor(targetMethod);
        int start = allocator.allocate(requiredSlots);
        body.shiftLocalVariables(start - body.parameterSlots());
    }

    private static void registerTryCatchBlocks(MethodNode targetMethod, PreparedBody body) {
        if (body.tryCatchBlocks().isEmpty()) {
            return;
        }

        if (targetMethod.tryCatchBlocks == null) {
            targetMethod.tryCatchBlocks = new ArrayList<>();
        }

        targetMethod.tryCatchBlocks.addAll(body.tryCatchBlocks());
    }
}
