package cn.xiaozhou233.juicemixin.internal.injection;

import cn.xiaozhou233.juicemixin.internal.bytecode.TargetContext;
import cn.xiaozhou233.juicemixin.internal.metadata.InjectionSpec;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * Finds the insertion positions of one injection point kind.
 */
public interface InjectionPoint {

    List<Insertion> find(TargetContext context, MethodNode targetMethod, InjectionSpec spec);
}
