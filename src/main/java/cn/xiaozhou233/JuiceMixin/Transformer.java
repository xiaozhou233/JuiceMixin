package cn.xiaozhou233.JuiceMixin;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;

import javassist.*;

public class Transformer {
    private static final Instrumentation inst = JuiceMixin.getInst();
    private static HashMap<Class<?>, CtClass> redefineMap = new HashMap<>();

    public static void HookClass(Class<?> hook, String className, String methodName)
            throws NotFoundException, CannotCompileException, UnmodifiableClassException, ClassNotFoundException, IOException {

        // 获取 Javassist 的类池
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(hook)); // 添加 hook 类路径

        // 获取目标类及方法
        CtClass cc = null;
        if(redefineMap.containsKey(Class.forName(className))){
            cc = redefineMap.get(Class.forName(className));
        }else {
            cc = pool.get(className);
            redefineMap.put(Class.forName(className),cc);
        }
        CtMethod method = cc.getDeclaredMethod(methodName);

        // 插入钩子逻辑
            method.insertAfter("{ try { " +
                    "Class.forName(\"" + hook.getName() + "\").getMethod(\"tick\", new Class[0]).invoke(null, (Object[]) null); " +
                    "} catch (Exception e) { e.printStackTrace(); } }");
    }

    public static void redefineClass(){
        for(Class<?> c : redefineMap.keySet()){
            try {
                CtClass cc = redefineMap.get(c);
                // 将修改后的类写回到 JVM
                inst.redefineClasses(new ClassDefinition(c, cc.toBytecode()));

                // Detach 避免内存泄漏
                cc.detach();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
