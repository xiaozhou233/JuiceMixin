package cn.xiaozhou233.juicemixin.operation;

import cn.xiaozhou233.juicemixin.annotations.At;
import cn.xiaozhou233.juicemixin.exception.MixinException;
import cn.xiaozhou233.juicemixin.utils.OpcodesMap;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Inserts a prepared {@link InjectionBody} into a target method.
 * <p>
 * Supported injection points:
 * <ul>
 *   <li>{@code HEAD} - first instruction, or first instruction after the
 *       {@code super(...)} call in a constructor.</li>
 *   <li>{@code TAIL} - before the last return instruction.</li>
 *   <li>{@code INVOKE} - method invocations matched by
 *       {@code owner.name(descriptor)}.</li>
 *   <li>{@code FIELD} - field accesses matched by
 *       {@code owner.name:descriptor}.</li>
 *   <li>{@code CONSTANT} - {@code LDC} constants matched by their value.</li>
 * </ul>
 * {@code HEAD} and {@code TAIL} insert once; member based points insert at every
 * match unless {@link At#ordinal()} selects a single one.
 */
public final class InjectOperation {

    private InjectOperation() {
    }

    /**
     * Injects {@code body} into {@code targetMethod}.
     *
     * @param targetMethod         the method to inject into
     * @param at                   the injection point
     * @param body                 a fresh body, owned by this injection
     * @param originalInstructions identity set of the target method instructions
     *                             that existed before any injection; member
     *                             matching ignores injected code
     * @throws MixinException if the injection point is invalid, unsupported or
     *                        does not match anything
     */
    public static void execute(
            MethodNode targetMethod,
            At at,
            InjectionBody body,
            Set<AbstractInsnNode> originalInstructions
    ) {
        Objects.requireNonNull(targetMethod, "targetMethod");
        Objects.requireNonNull(at, "at");
        Objects.requireNonNull(body, "body");
        Objects.requireNonNull(originalInstructions, "originalInstructions");

        String point = at.value() == null
                ? ""
                : at.value().trim().toUpperCase(Locale.ROOT);

        switch (point) {
            case "HEAD":
                requireDefaultShift(at, point);
                injectAtHead(targetMethod, body);
                break;
            case "TAIL":
                requireDefaultShift(at, point);
                injectAtTail(targetMethod, body);
                break;
            case "INVOKE":
            case "FIELD":
            case "CONSTANT":
                injectAtMatches(targetMethod, at, body, point, originalInstructions);
                break;
            default:
                if (OpcodesMap.contains(point)) {
                    throw new MixinException(
                            "'" + at.value() + "' is a JVM instruction name, not an injection point"
                                    + " (target method " + targetMethod.name + targetMethod.desc
                                    + "); use INVOKE, FIELD or CONSTANT to match instructions"
                    );
                }
                throw new MixinException(
                        "Unknown injection point '" + at.value() + "' (target method "
                                + targetMethod.name + targetMethod.desc
                                + "); expected HEAD, TAIL, INVOKE, FIELD or CONSTANT"
                );
        }
    }

    private static void requireDefaultShift(At at, String point) {
        if (at.shift() != At.Shift.BEFORE) {
            throw new MixinException(
                    "@At(shift = AFTER) is not supported for " + point
                            + "; HEAD and TAIL use the default shift"
            );
        }
    }

    private static void injectAtHead(MethodNode targetMethod, InjectionBody body) {
        AbstractInsnNode anchor = firstInstruction(targetMethod);

        if ("<init>".equals(targetMethod.name)) {
            // 'this' is uninitialized before the super(...) / this(...) call, so
            // injected code may only run after it.
            anchor = constructorCall(targetMethod);
            targetMethod.instructions.insert(anchor, body.getInstructions());
        } else {
            targetMethod.instructions.insertBefore(anchor, body.getInstructions());
        }

        registerTryCatchBlocks(targetMethod, body);
    }

    private static void injectAtTail(MethodNode targetMethod, InjectionBody body) {
        AbstractInsnNode lastReturn = null;

        for (AbstractInsnNode instruction = targetMethod.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (isReturn(instruction.getOpcode())) {
                lastReturn = instruction;
            }
        }

        if (lastReturn == null) {
            throw new MixinException(
                    "Cannot inject at TAIL: target method " + targetMethod.name
                            + targetMethod.desc + " has no return instruction"
            );
        }

        targetMethod.instructions.insertBefore(lastReturn, body.getInstructions());
        registerTryCatchBlocks(targetMethod, body);
    }

    private static void injectAtMatches(
            MethodNode targetMethod,
            At at,
            InjectionBody body,
            String point,
            Set<AbstractInsnNode> originalInstructions
    ) {
        List<AbstractInsnNode> matches = findMatches(
                targetMethod, at.target(), point, originalInstructions
        );

        if (matches.isEmpty()) {
            throw new MixinException(
                    "No " + point + " match for target '" + at.target() + "' in "
                            + targetMethod.name + targetMethod.desc
            );
        }

        List<AbstractInsnNode> selected = select(matches, at.ordinal(), at.target(), point,
                targetMethod);

        for (int i = 0; i < selected.size(); i++) {
            // Every insertion needs its own instruction list: an InsnList can
            // only be linked into one place.
            InjectionBody insertion = i == 0 ? body : body.copy();
            AbstractInsnNode match = selected.get(i);

            if (at.shift() == At.Shift.AFTER) {
                targetMethod.instructions.insert(match, insertion.getInstructions());
            } else {
                targetMethod.instructions.insertBefore(match, insertion.getInstructions());
            }

            registerTryCatchBlocks(targetMethod, insertion);
        }
    }

    private static List<AbstractInsnNode> findMatches(
            MethodNode targetMethod,
            String wanted,
            String point,
            Set<AbstractInsnNode> originalInstructions
    ) {
        String target = wanted == null ? "" : wanted;
        List<AbstractInsnNode> matches = new ArrayList<>();

        for (AbstractInsnNode instruction = targetMethod.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (!originalInstructions.contains(instruction)) {
                continue;
            }

            if (matches(instruction, point, target)) {
                matches.add(instruction);
            }
        }

        return matches;
    }

    private static boolean matches(AbstractInsnNode instruction, String point, String target) {
        if ("INVOKE".equals(point)) {
            if (!(instruction instanceof MethodInsnNode)) {
                return false;
            }

            MethodInsnNode call = (MethodInsnNode) instruction;
            return target.isEmpty()
                    || target.equals(call.owner + "." + call.name + call.desc);
        }

        if ("FIELD".equals(point)) {
            if (!(instruction instanceof FieldInsnNode)) {
                return false;
            }

            FieldInsnNode field = (FieldInsnNode) instruction;
            return target.isEmpty()
                    || target.equals(field.owner + "." + field.name + ":" + field.desc);
        }

        if ("CONSTANT".equals(point)) {
            if (!(instruction instanceof LdcInsnNode)) {
                return false;
            }

            return target.isEmpty()
                    || matchesConstant(((LdcInsnNode) instruction).cst, target);
        }

        return false;
    }

    private static boolean matchesConstant(Object constant, String target) {
        if (constant instanceof String) {
            return target.equals(constant);
        }

        if (constant instanceof Type) {
            Type type = (Type) constant;

            if (target.equals(type.getDescriptor()) || target.equals(type.getClassName())) {
                return true;
            }

            return type.getSort() == Type.OBJECT && target.equals(type.getInternalName());
        }

        return target.equals(String.valueOf(constant));
    }

    private static List<AbstractInsnNode> select(
            List<AbstractInsnNode> matches,
            int ordinal,
            String target,
            String point,
            MethodNode targetMethod
    ) {
        if (ordinal < 0) {
            return matches;
        }

        if (ordinal >= matches.size()) {
            throw new MixinException(
                    "Ordinal " + ordinal + " is out of range for " + point + " target '"
                            + target + "' in " + targetMethod.name + targetMethod.desc
                            + ": only " + matches.size() + " match(es)"
            );
        }

        return Collections.singletonList(matches.get(ordinal));
    }

    private static AbstractInsnNode firstInstruction(MethodNode targetMethod) {
        AbstractInsnNode first = targetMethod.instructions.getFirst();
        if (first == null) {
            throw new MixinException(
                    "Cannot inject into target method " + targetMethod.name
                            + targetMethod.desc + ": it has no instructions"
            );
        }
        return first;
    }

    private static AbstractInsnNode constructorCall(MethodNode constructor) {
        for (AbstractInsnNode instruction = constructor.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() == Opcodes.INVOKESPECIAL && "<init>".equals(call.name)) {
                return call;
            }
        }

        throw new MixinException(
                "Cannot inject at HEAD of constructor " + constructor.desc
                        + ": no super(...) or this(...) call found"
        );
    }

    private static void registerTryCatchBlocks(MethodNode targetMethod, InjectionBody body) {
        if (body.getTryCatchBlocks().isEmpty()) {
            return;
        }

        if (targetMethod.tryCatchBlocks == null) {
            targetMethod.tryCatchBlocks = new ArrayList<>();
        }

        targetMethod.tryCatchBlocks.addAll(body.getTryCatchBlocks());
    }

    private static boolean isReturn(int opcode) {
        return opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
    }
}
