package domino.java.capsule;

/**
 * A capsule scope implementation based on a {@link Iterable}.
 *
 */
public class DefaultCapsuleScope implements CapsuleScope {
	private final Iterable<Capsule> capsules;

	/**
	 * Creates a capsule scope containing the given capsules.
	 * 
	 * @param capsules
	 *            The capsules in this scope.
	 */
	public DefaultCapsuleScope(final Iterable<Capsule> capsules) {
		this.capsules = capsules;
	}

	/**
	 * Stop this stop and therefore all containing capsules.
	 */
	@Override
	public void stop() {
		for (final Capsule capsule : capsules) {
			capsule.stop();
		}
	}
}