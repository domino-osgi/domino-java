package domino.java;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import de.tototec.utils.functional.Optional;
import de.tototec.utils.functional.Procedure1;
import domino.java.capsule.Capsule;
import domino.java.internal.Logger;
import domino.java.internal.LoggerFactory;

/**
 * A capsule which executes the given event handlers on service state
 * transitions while the current scope is active. Tracks all state transitions
 * and services visible to the class loader. The custom object facility of the
 * service tracker is not used.
 *
 * @param filter
 *            Filter expression restricting the set of services to be tracked
 * @param f
 *            Event handlers
 * @param bundleContext
 *            Bundle context
 * @tparam S Service type to be tracked
 */
public class ServiceWatcherCapsule<S> implements Capsule {

	private final Logger log = LoggerFactory.getLogger(ServiceWatcherCapsule.class);

	private final Filter filter;
	private final Procedure1<ServiceWatcherEvent<S>> f;
	private final BundleContext bundleContext;

	private Optional<ServiceTracker<S, S>> tracker = Optional.none();

	public ServiceWatcherCapsule(
			final Filter filter,
			final Procedure1<ServiceWatcherEvent<S>> f,
			final BundleContext bundleContext) {
		this.filter = filter;
		this.f = f;
		this.bundleContext = bundleContext;
	}

	public Optional<ServiceTracker<S, S>> tracker() {
		return tracker;
	}

	@Override
	public void start() {
		if (log.isDebugEnabled()) {
			log.debug("Bundle {}: Start tracking services with filter [{}]", Util.bundleName(bundleContext), filter);
		}

		// Create tracker matching this filter
		final ServiceTracker<S, S> t = new ServiceTracker<S, S>(bundleContext, filter, null) {
			@Override
			public S addingService(final ServiceReference<S> reference) {
				final S service = context.getService(reference);
				if (log.isDebugEnabled()) {
					log.debug("Bundle {}: Adding service [{}] for filter [{}]", Util.bundleName(bundleContext), service,
							filter);
				}
				final ServiceWatcherEvent<S> event = new ServiceWatcherEvent<S>(
						service,
						new ServiceWatcherContext<>(tracker().orNull(), reference),
						ServiceWatcherEvent.EventType.ADDING);
				f.apply(event);
				return service;
			}

			@Override
			public void modifiedService(final ServiceReference<S> reference, final S service) {
				if (log.isDebugEnabled()) {
					log.debug("Bundle {}: Modified service [{}] for filter [{}]", Util.bundleName(bundleContext),
							service, filter);
				}
				final ServiceWatcherEvent<S> event = new ServiceWatcherEvent<S>(
						service,
						new ServiceWatcherContext<>(tracker().orNull(), reference),
						ServiceWatcherEvent.EventType.MODIFIED);
				f.apply(event);
			}

			@Override
			public void removedService(final ServiceReference<S> reference, final S service) {
				if (log.isDebugEnabled()) {
					log.debug("Bundle {}: Removed service [{}] for filter [{}]", Util.bundleName(bundleContext),
							service, filter);
				}
				final ServiceWatcherEvent<S> event = new ServiceWatcherEvent<S>(
						service,
						new ServiceWatcherContext<>(tracker().orNull(), reference),
						ServiceWatcherEvent.EventType.REMOVED);
				try {
					f.apply(event);
				} finally {
					context.ungetService(reference);
				}
			}
		};
		tracker = Optional.some(t);
		t.open();
	}

	@Override
	public void stop() {
		if (log.isDebugEnabled()) {
			log.debug("Bundle {}: Stop tracking services with filter [{}]", Util.bundleName(bundleContext), filter);
		}
		tracker.foreach(t -> {
			t.close();
			tracker = Optional.none();
		});
	}

}
