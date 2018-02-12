package domino.java;

/**
 * A Service watcher events.
 * 
 * The possible events types are defined by {@link EventType} an can be
 * retrieved via {@link #eventType()}.
 * 
 * @param service
 *            Service affected by the state transition.
 * @param context
 *            Additional event data.
 */
public class ServiceWatcherEvent<S> {

	/**
	 * Contains the possible service watcher event types.
	 */
	public static enum EventType {
		/** A service is being added to the ServiceTracker. */
		ADDING,
		/** A service tracked by the ServiceTracker has been modified. */
		MODIFIED,
		/** A service tracked by the ServiceTracker has been removed. */
		REMOVED;
	}

	private final S service;
	private final ServiceWatcherContext<S> context;
	private final EventType eventType;

	public ServiceWatcherEvent(
			final S service,
			final ServiceWatcherContext<S> context,
			final EventType eventType) {
		this.service = service;
		this.context = context;
		this.eventType = eventType;
	}

	public S service() {
		return service;
	}

	public ServiceWatcherContext<S> context() {
		return context;
	}

	public EventType eventType() {
		return eventType;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +
				"(service=" + service +
				",context=" + context +
				",eventType=" + eventType +
				")";
	}

}
