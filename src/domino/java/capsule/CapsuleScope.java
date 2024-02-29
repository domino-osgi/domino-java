package domino.java.capsule;

/**
 * Represents a collection of capsules which shall all be stopped on the same
 * event.
 */
public interface CapsuleScope {
	/**
	 * Stops all capsules in this scope.
	 */
	void stop();
}
