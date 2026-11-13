package cn.xiaozhou233.juicemixin.exception;

/**
 * Thrown when a mixin cannot be applied, for example because the target method
 * does not exist, the target bytecode is missing, or an injection point is
 * unknown.
 */
public class MixinException extends RuntimeException {

    public MixinException(String message) {
        super(message);
    }

    public MixinException(String message, Throwable cause) {
        super(message, cause);
    }
}
