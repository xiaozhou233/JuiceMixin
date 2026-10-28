package cn.xiaozhou233.juicemixin.data.annotations;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MixinData {
    private final Class<?> value;
}
