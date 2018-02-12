package domino.java;

import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Contains details about the current service event. Might be expanded in
 * future.
 *
 * @param tracker
 *            Underlying service tracker
 * @param reference
 *            Service reference
 */
public class ServiceWatcherContext<S> {

	private final ServiceTracker<S, S> tracker;
	private final ServiceReference<S> reference;

	public ServiceWatcherContext(final ServiceTracker<S, S> tracker, final ServiceReference<S> reference) {
		this.tracker = tracker;
		this.reference = reference;
	}

	public ServiceReference<S> reference() {
		return reference;
	}

	public ServiceTracker<S, S> tracker() {
		return tracker;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +
				"(tracker=" + tracker +
				",reference=" + reference +
				")";
	}

}
