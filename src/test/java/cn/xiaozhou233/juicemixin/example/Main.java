package cn.xiaozhou233.juicemixin.example;

import cn.xiaozhou233.juicemixin.JuiceMixin;
import cn.xiaozhou233.juicemixin.MixinConfig;
import cn.xiaozhou233.juicemixin.MixinTransformer;
import cn.xiaozhou233.juicemixin.provider.impl.FileOutputProvider;
import cn.xiaozhou233.juicemixin.provider.impl.MixinClassLoader;

import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        MixinConfig config = MixinConfig.builder()
                .outputProvider(new FileOutputProvider())
                .build();

        MixinTransformer transformer = JuiceMixin.transformer(config);
        Map<String, byte[]> transformed = transformer.transform(TargetMixin.class);

        MixinClassLoader loader = new MixinClassLoader(Main.class.getClassLoader());
        for (Map.Entry<String, byte[]> entry : transformed.entrySet()) {
            loader.defineClass(entry.getKey(), entry.getValue());
        }

        Class<?> targetClass = loader.getDefinedClass(Target.class.getName());
        Object target = targetClass.getDeclaredConstructor().newInstance();
        targetClass.getMethod("run").invoke(target);
    }
}
