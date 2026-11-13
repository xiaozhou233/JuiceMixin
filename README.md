# JuiceMixin

一个基于 ASM 的轻量运行时 Mixin 库。它读取 mixin 类字节码中的注解，把 handler 方法体合并进目标方法，并重新计算栈帧后输出修改后的类。

## 快速开始

```java
MixinTransformer transformer = JuiceMixin.transformer(
        MixinConfig.builder()
                .outputProvider(new FileOutputProvider()) // 不设置则默认不输出
                .build()
);

Map<String, byte[]> classes = transformer.transform(MyMixin.class);
```

也可以直接使用默认配置：

```java
Map<String, byte[]> classes = JuiceMixin.transform(MyMixin.class);
```

## 编写 Mixin

```java
@Mixin(Target.class)
public class TargetMixin {

    @Inject(method = "run", desc = "()V")
    private void onRun() {
        System.out.println("before return");
    }
}
```

### 注入点

| `@At.value` | 说明 |
| --- | --- |
| `TAIL` | 目标方法最后一个 return 之前（默认） |
| `HEAD` | 方法开头；构造函数中位于 `super(...)` / `this(...)` 之后 |
| `INVOKE` | 匹配方法调用，格式 `owner.name(descriptor)` |
| `FIELD` | 匹配字段访问，格式 `owner.name:descriptor` |
| `CONSTANT` | 匹配 `LDC` 常量 |

```java
// 在 println 调用前插入
@At(value = "INVOKE",
     target = "java/io/PrintStream.println(Ljava/lang/String;)V")

// 在字段读取前插入
@At(value = "FIELD",
     target = "com/example/Target.value:I")

// 在字符串常量加载前插入
@At(value = "CONSTANT", target = "Hello")

// 只匹配第二个 println，并插到它后面
@At(value = "INVOKE",
     target = "java/io/PrintStream.println(Ljava/lang/String;)V",
     ordinal = 1,
     shift = At.Shift.AFTER)
```

- `target` 为空表示匹配该类别的所有指令。
- `ordinal` 从 0 开始，`-1`（默认）表示所有匹配都注入。
- `shift` 支持 `BEFORE` / `AFTER`；`HEAD` 和 `TAIL` 只支持 `BEFORE`。
- `@Inject` 可重复，一个 handler 可以注入到多个目标方法 / 多个位置。

## 扩展点

`MixinConfig` 中的 provider 都可以替换：

- `BytecodeProvider`：读取类字节码。
- `ClassProvider`：按 internal name 解析类，用于目标类解析和栈帧公共父类计算。
- `OutputProvider`：接收转换后的类，默认 `EmptyOutputProvider`。
- `LoggerProvider`：日志输出。

## 结构

```text
cn.xiaozhou233.juicemixin
├── JuiceMixin            // 静态门面
├── MixinTransformer      // 公共入口
├── MixinConfig           // provider 配置
├── annotations           // 稳定 API：@Mixin / @Inject / @At
├── exception
├── provider              // 扩展点接口与默认实现
└── internal
    ├── metadata          // 注解解析、内部模型
    ├── bytecode          // ClassNode、栈帧、body 克隆、局部变量分配
    ├── injection         // 注入点策略与插入逻辑
    └── transform         // target 编排、兼容性校验
```

`internal` 下的类不属于公共 API，可能随实现调整。

## 限制

- `INVOKE` 不匹配 `invokedynamic`。
- `CONSTANT` 只匹配 `LDC`，编译器生成成 `ICONST_*` / `BIPUSH` / `SIPUSH` 的小整数不会命中。
- 目标类必须能被配置的 `ClassProvider` 解析。
- 当前要求 target 与 handler 的 static 属性一致，handler 参数必须是 target 参数的前缀。

## 构建

```bash
./gradlew test
./gradlew build
```
