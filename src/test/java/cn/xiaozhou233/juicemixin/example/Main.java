package cn.xiaozhou233.juicemixin.example;

import cn.xiaozhou233.juicemixin.JuiceMixin;
import cn.xiaozhou233.juicemixin.Transformer;
import cn.xiaozhou233.juicemixin.provider.impl.ClassLoaderOutputProvider;
import cn.xiaozhou233.juicemixin.provider.impl.FileOutputProvider;
import cn.xiaozhou233.juicemixin.registries.MixinRegistry;

import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        JuiceMixin.init();
        JuiceMixin.setOutputProvider(new FileOutputProvider());

        MixinRegistry mixinRegistry = new MixinRegistry();
        mixinRegistry.addMixin(TargetMixin.class);

        // transform() writes the classes to ./bytes and returns them, so the
        // result can be loaded and executed right away.
        Map<String, byte[]> transformed = new Transformer(mixinRegistry)
                .setOutputProviderEnabled(true)
                .transform();

        ClassLoaderOutputProvider.CustomClassloader loader =
                new ClassLoaderOutputProvider.CustomClassloader(Main.class.getClassLoader());

        for (Map.Entry<String, byte[]> entry : transformed.entrySet()) {
            loader.defineClass(entry.getKey(), entry.getValue());
        }

        Class<?> targetClass = loader.getDefinedClass(Target.class.getName());
        Object target = targetClass.getDeclaredConstructor().newInstance();
        targetClass.getMethod("run").invoke(target);
    }
}
