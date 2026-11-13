package cn.xiaozhou233.juicemixin.internal.injection;

import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.internal.bytecode.TargetContext;
import cn.xiaozhou233.juicemixin.internal.metadata.InjectionSpec;
import cn.xiaozhou233.juicemixin.internal.metadata.InsertionShift;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for injection points that match instructions by a member signature.
 */
public abstract class MemberInjectionPoint implements InjectionPoint {

    @Override
    public List<Insertion> find(
            TargetContext context,
            MethodNode targetMethod,
            InjectionSpec spec
    ) {
        List<AbstractInsnNode> matches = new ArrayList<>();

        for (AbstractInsnNode instruction = targetMethod.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (!context.originalInstructions().contains(instruction)) {
                continue;
            }

            if (matches(instruction, spec.target())) {
                matches.add(instruction);
            }
        }

        if (matches.isEmpty()) {
            throw new MixinException(
                    "No " + spec.kind() + " match for target '" + spec.target() + "' in "
                            + targetMethod.name + targetMethod.desc
            );
        }

        List<AbstractInsnNode> selected = select(matches, spec, targetMethod);
        List<Insertion> insertions = new ArrayList<>();

        for (AbstractInsnNode match : selected) {
            insertions.add(spec.shift() == InsertionShift.AFTER
                    ? Insertion.after(match)
                    : Insertion.before(match));
        }

        return insertions;
    }

    protected abstract boolean matches(AbstractInsnNode instruction, String target);

    private static List<AbstractInsnNode> select(
            List<AbstractInsnNode> matches,
            InjectionSpec spec,
            MethodNode targetMethod
    ) {
        if (spec.ordinal() < 0) {
            return matches;
        }

        if (spec.ordinal() >= matches.size()) {
            throw new MixinException(
                    "Ordinal " + spec.ordinal() + " is out of range for "
                            + spec.kind() + " target '" + spec.target() + "' in "
                            + targetMethod.name + targetMethod.desc + ": only "
                            + matches.size() + " match(es)"
            );
        }

        return Collections.singletonList(matches.get(spec.ordinal()));
    }
}
