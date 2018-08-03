package domino.java;

import org.osgi.util.tracker.ServiceTracker;

import de.tototec.utils.functional.Procedure1;
import de.tototec.utils.functional.Procedure2;
import de.tototec.utils.functional.Procedure3;
import de.tototec.utils.functional.Procedure4;

/**
 * Provides convenient methods to add a service watcher to the current scope or
 * wait until services are present.
 */
public interface ServiceWatching {

	/**
	 * Lets you react to service events for services with the specified type which
	 * match the given filter.
	 *
	 * @param f
	 *            Service event handler
	 * @tparam S Service type
	 * @return Underlying service tracker
	 */
	<S> ServiceTracker<S, S> watchAdvancedServices(Class<S> type, String filter, Procedure1<ServiceWatcherEvent<S>> f);

	/**
	 * Activates the given inner logic as long as the first service of the given
	 * type is present. This implements the concept of required services. The inner
	 * logic is started as soon as a service s of the given type gets present and
	 * stopped when s is removed.
	 */
	<S> ServiceTracker<S, S> whenAdvancedServicePresent(Class<S> type, String filter, Procedure1<S> f);

	/**
	 * Waits until a service of the specified type is available and executes the
	 * given event handler with it. When the service disappears, the capsules added
	 * in the handlers are stopped. You can wait on a bunch of services if you nest
	 * `whenServicePresent` methods.
	 *
	 * @param f
	 *            Handler
	 * @tparam S Service type
	 * @return Underlying service tracker
	 */
	<S> ServiceTracker<S, S> whenServicePresent(Class<S> type, Procedure1<S> f);

	<S1, S2> ServiceTracker<S1, S1> whenServicesPresent(Class<S1> type1, Class<S2> type2, Procedure2<S1, S2> f);

	<S1, S2, S3> ServiceTracker<S1, S1> whenServicesPresent(
			Class<S1> type1,
			Class<S2> type2,
			Class<S3> type3,
			Procedure3<S1, S2, S3> f);

	<S1, S2, S3, S4> ServiceTracker<S1, S1> whenServicesPresent(
			final Class<S1> type1,
			final Class<S2> type2,
			final Class<S3> type3,
			final Class<S4> type4,
			final Procedure4<S1, S2, S3, S4> f);
}
