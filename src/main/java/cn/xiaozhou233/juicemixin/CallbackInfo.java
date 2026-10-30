package cn.xiaozhou233.juicemixin;

/**
 * Optional final parameter of an injector method, used to cancel the target method.
 *
 * <p>Only meaningful when the injection point is {@code HEAD} (see
 * {@link cn.xiaozhou233.juicemixin.annotations.At#value()}), {@code INVOKE},
 * {@code FIELD} or {@code CONSTANT}; using it on {@code RETURN} / {@code TAIL}
 * is illegal.
 *
 * <ul>
 *   <li>Target method returns {@code void}: call {@link #cancel()};</li>
 *   <li>Target method returns non-{@code void}: call {@link #cancel(Object)} with
 *       the return value;</li>
 *   <li>Cancelling at {@code HEAD} means the target method body is not executed.</li>
 * </ul>
 */
public final class CallbackInfo {

    private boolean cancelled;
    private Object returnValue;

    /** Cancels execution of the target method. If the target returns non-{@code void}, use {@link #cancel(Object)} instead. */
    public void cancel() {
        this.cancelled = true;
    }

    /** Cancels execution of the target method and makes it return {@code returnValue}. */
    public void cancel(Object returnValue) {
        this.cancelled = true;
        this.returnValue = returnValue;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    /** Framework-internal: the value the target method should return when cancelled. */
    public Object getReturnValue() {
        return returnValue;
    }
}
