package domino.java.capsule;

/**
 * Represents the tree structure which is formed by capsule scopes and their
 * contained capsules.
 */
public interface CapsuleContext {
	/**
	 * Starts the given capsule and adds it to the current capsule scope.
	 *
	 * @param capsule
	 *            The capsule to add.
	 */
	void addCapsule(Capsule capsule);

	/**
	 * Creates a new capsule scope on top of the active one and executes the given
	 * function in it.
	 * So the function sees the new capsule scope as the current one.
	 *
	 * @param f
	 *            The function which might add capsules to the new scope.
	 * @return The new scope.
	 */
	CapsuleScope executeWithinNewCapsuleScope(Runnable f);
}