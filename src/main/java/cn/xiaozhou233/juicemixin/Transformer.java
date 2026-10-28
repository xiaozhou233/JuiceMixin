package cn.xiaozhou233.juicemixin;

import cn.xiaozhou233.juicemixin.utils.ASMUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Transformer {
    private final MixinRegistry mixinRegistry;
    private final List<MixinContext> mixinContexts = new ArrayList<>();
    private final HashMap<String, byte[]> originalBytes = new HashMap<>();
    private boolean enabledOutputProvider = false;

    public Transformer(MixinRegistry mixinRegistry) {
        this.mixinRegistry = mixinRegistry;

        for (Class<?> mixin : mixinRegistry.getMixinList()) {
            byte[] mixinBytes = JuiceMixin.getBytecodeProvider().get(mixin);
            if (mixinBytes == null || mixinBytes.length == 0) {
                JuiceMixin.getLoggerProvider().error("Cannot to get the bytecode of Mixin {}", mixin.getName());
                continue;
            }
            MixinContext context = new MixinContext(ASMUtils.toClassNode(mixinBytes));
            mixinContexts.add(context);
        }
    }

    public Transformer setOutputProviderEnabled(boolean enabled) {
        this.enabledOutputProvider = enabled;
        return this;
    }

    public HashMap<String, byte[]> transform() {
        // TODO: implement this.

        return null;
    }
}
