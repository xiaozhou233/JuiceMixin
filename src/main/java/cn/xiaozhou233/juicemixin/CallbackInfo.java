package cn.xiaozhou233.juicemixin;

/**
 * 注入方法的可选最后一个参数，用来取消目标方法。
 *
 * <p>只有注入点为 {@link cn.xiaozhou233.juicemixin.annotations.At.Kind#HEAD}、
 * {@code INVOKE}、{@code FIELD}、{@code CONSTANT} 时才有意义；
 * 用在 {@code RETURN} / {@code TAIL} 上属于非法用法。
 *
 * <ul>
 *   <li>目标方法返回 {@code void}：调用 {@link #cancel()}；</li>
 *   <li>目标方法返回非 {@code void}：调用 {@link #cancel(Object)} 指定返回值；</li>
 *   <li>在 {@code HEAD} 上取消，目标方法体不会执行。</li>
 * </ul>
 */
public final class CallbackInfo {

    private boolean cancelled;
    private Object returnValue;

    /** 取消目标方法的执行。目标方法返回非 {@code void} 时应改用 {@link #cancel(Object)}。 */
    public void cancel() {
        this.cancelled = true;
    }

    /** 取消目标方法的执行，并让目标方法返回 {@code returnValue}。 */
    public void cancel(Object returnValue) {
        this.cancelled = true;
        this.returnValue = returnValue;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    /** 框架内部使用：取消时目标方法应返回的值。 */
    public Object getReturnValue() {
        return returnValue;
    }
}
