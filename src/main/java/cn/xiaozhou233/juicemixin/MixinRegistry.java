
package cn.xiaozhou233.juicemixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MixinRegistry {
    private final List<Class<?>> mixinList = new ArrayList<>();

    public void addMixin(Class<?>... mixins) {
        Collections.addAll(mixinList, mixins);
    }

    public void clearMixin() {
        mixinList.clear();
    }

    public boolean removeMixin(Class<?> mixin) {
        return mixinList.remove(mixin);
    }

    public boolean containsMixin(Class<?> mixin) {
        return mixinList.contains(mixin);
    }

    public int size() {
        return mixinList.size();
    }

    public List<Class<?>> getMixinList() {
        return Collections.unmodifiableList(mixinList);
    }
}
