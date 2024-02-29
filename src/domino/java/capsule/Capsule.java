package domino.java.capsule;

/**
 * A capsule represents something which is startable and stoppable.
 * 
 * After being started and before being stopped, the capsule is active.
 * Otherwise, it's inactive.
 */
public interface Capsule {
	/**
	 * Starts the capsule.
	 * After that, the capsule is active.
	 */
	void start();

	/**
	 * Stops the capsule.
	 * After that, the capsule is inactive.
	 */
	void stop();
}