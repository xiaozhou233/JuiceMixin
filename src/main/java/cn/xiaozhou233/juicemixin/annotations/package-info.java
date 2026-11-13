/**
 * Annotations that describe how mixin code is applied to an already loaded class.
 * <p>
 * A mixin class is annotated with {@link cn.xiaozhou233.juicemixin.annotations.Mixin}
 * to declare its target, and each handler method inside it is annotated with
 * {@link cn.xiaozhou233.juicemixin.annotations.Inject} to declare the target
 * method plus the injection point. {@link cn.xiaozhou233.juicemixin.annotations.At}
 * refines where inside that method the code is placed.
 * <p>
 * All three annotations are retained at runtime, and all three ship a nested
 * {@code Helper} class that rebuilds the annotation instances from bytecode, so
 * the same declarations can be read without reflecting on the mixin class.
 */
package cn.xiaozhou233.juicemixin.annotations;
