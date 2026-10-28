package cn.xiaozhou233.juicemixin.provider.impl;

import cn.xiaozhou233.juicemixin.provider.OutputProvider;

public class EmptyOutputProvider implements OutputProvider {
    @Override
    public void output(String name, byte[] bytes) {
        // Keep it empty
    }
}
