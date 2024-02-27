package domino.java.capsule;

import de.tototec.utils.functional.F0;

/**
 * `DynamicVariable` provides a binding mechanism where the current value is
 * found through dynamic scope, but where access to the variable itself is
 * resolved through static scope.
 * <p>
 * The current value can be retrieved with the {@link #value()} method.
 * New values should be pushed using the {@link #withValue(Object, F0)} method.
 * Values pushed via {@link #withValue(Object, F0)} only stay valid while the
 * {@link #withValue(Object, F0)}'s second argument, a parameterless closure,
 * executes.
 * When the second argument finishes, the variable reverts to the previous
 * value.
 *
 * <pre>
 * someDynamicVariable.withValue(newValue, () -> {
 * // ... code called in here that calls value ...
 * // ... will be given back the newValue ...
 * })
 *
 * </pre>
 * <p>
 * Each thread gets its own stack of bindings.
 * When a new thread is created, the `DynamicVariable` gets a copy of the stack
 * of bindings from the parent thread, and from then on the bindings for the new
 * thread are independent of those for the original thread.
 * <p>
 * This class was ported from Scala 2.11 (`scala.util.DynamicVariable`), which
 * as orginally written by Lex Spoon.
 */
public class DynamicVariable<T> {

	private final T init;

	private InheritableThreadLocal<T> tl = new InheritableThreadLocal<T>() {
		protected T initialValue() {
			return init;
		}
	};

	public DynamicVariable(final T init) {
		this.init = init;
	}

	/**
	 * Retrieve the current value.
	 */
	public T value() {
		return tl.get();
	}

	/**
	 * Set the value of the variable while executing the specified thunk.
	 *
	 * @param newval The value to which to set the variable.
	 * @param thunk  The code to evaluate under the new setting.
	 */
	public <S> S withValue(final T newVal, final F0<S> thunk) {
		final T oldVal = value();
		tl.set(newVal);
		try {
			return thunk.apply();
		} finally {
			tl.set(oldVal);
		}
	}

	/**
	 * Change the currently bound value, discarding the old value. Usually
	 * {@link #withValue(Object, F0)} gives better semantics.
	 */
	public void setValue(final T newVal) {
		tl.set(newVal);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + value() + ")";
	}
}
