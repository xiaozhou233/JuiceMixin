package cn.xiaozhou233.juicemixin.example;

import cn.xiaozhou233.juicemixin.JuiceMixin;
import cn.xiaozhou233.juicemixin.MixinRegistry;
import cn.xiaozhou233.juicemixin.Transformer;
import cn.xiaozhou233.juicemixin.provider.impl.FileOutputProvider;

public class Main {
    public static void main(String[] args) {
        JuiceMixin.init(null, null, null);
        JuiceMixin.setOutputProvider(new FileOutputProvider());

        MixinRegistry mixinRegistry = new MixinRegistry();
        mixinRegistry.addMixin(TargetMixin.class);

        new Transformer(mixinRegistry)
                .setOutputProviderEnabled(true)
                .transform();
    }
}
