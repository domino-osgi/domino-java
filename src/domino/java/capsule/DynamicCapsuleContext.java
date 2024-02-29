package domino.java.capsule;

import java.util.LinkedHashSet;

import de.tototec.utils.functional.Optional;

/**
 * A {@link CapsuleContext} implementation based on {@link DynamicVariable} and
 * {@link DefaultCapsuleScope}.
 */
public class DynamicCapsuleContext implements CapsuleContext {
	/**
	 * A Set representing the current scope.
	 */
	private DynamicVariable<Optional<LinkedHashSet<Capsule>>> dynamicCapsuleSet = new DynamicVariable<Optional<LinkedHashSet<Capsule>>>(
			Optional.none());

	//
	@Override
	public void addCapsule(final Capsule capsule) {
		// Start the capsule immediately
		capsule.start();

		// Add capsule to the current set if there is one
		dynamicCapsuleSet.value().foreach(p -> p.add(capsule));
	}

	@Override
	public CapsuleScope executeWithinNewCapsuleScope(final Runnable f) {
		// Create the new set of capsules
		final LinkedHashSet<Capsule> newCapsuleSet = new LinkedHashSet<Capsule>();

		// // Execute the function in the new set
		dynamicCapsuleSet.withValue(Optional.some(newCapsuleSet), () -> {
			f.run();
			return null;
		});

		// Returns the set wrapped in the scope interface
		return new DefaultCapsuleScope(newCapsuleSet);
	}
}