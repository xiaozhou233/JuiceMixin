package cn.xiaozhou233.juicemixin.example;

import cn.xiaozhou233.juicemixin.JuiceMixin;
import cn.xiaozhou233.juicemixin.annotations.Inject;
import cn.xiaozhou233.juicemixin.annotations.Mixin;
import cn.xiaozhou233.juicemixin.test.TargetClassMixin;
import cn.xiaozhou233.juicemixin.utils.ASMUtils;
import cn.xiaozhou233.juicemixin.utils.AnnotationUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        byte[] bytes = JuiceMixin.getBytecodeProvider().get(TargetClassMixin.class);
        ClassNode classNode = ASMUtils.toClassNode(bytes);
        System.out.println(AnnotationUtils.hasAnnotation(classNode, Mixin.class));
        for (MethodNode method : classNode.methods) {
            for (Inject annotation : Inject.Helper.getAnnotations(method)) {
                System.out.println(annotation.method() + " " + annotation.desc() + " " + annotation.at().shift());
            }
        }
    }
}
