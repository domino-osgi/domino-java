package domino.java;

import java.util.Collection;
import java.util.List;

import org.osgi.framework.ServiceReference;

import de.tototec.utils.functional.F1;
import de.tototec.utils.functional.Optional;

/**
 * Provides convenient methods to consume OSGi services.
 */
public interface ServiceConsuming {

	/**
	 * Executes the given handler with the highest-ranked service of the specified
	 * type. If it's not available, it still executes it but with `None`. Doesn't
	 * take type parameters into account!
	 *
	 * When the handler returns, the service is released using
	 * {@link org.osgi.framework.BundleContext#ungetService(org.osgi.framework.ServiceReference)}.
	 *
	 * @param f
	 *            Handler that uses the service.
	 * @tparam S The service type.
	 * @tparam R The function result type.
	 * @return The handler result.
	 */
	<S, R> R withService(Class<S> type, F1<Optional<S>, R> f);

	/**
	 * Like {@link #service(Class)} but returns the reference so you can access meta
	 * information
	 * about that service. An implicit conversion adds a `service` property to the
	 * reference, so you can simply use that to obtain the service. Doesn't take
	 * type parameters into account!
	 *
	 */
	<S> Optional<ServiceReference<S>> serviceRef(Class<? super S> type);

	/**
	 * Like {@link #service(Class, String)} with filter but returns the service
	 * reference.
	 *
	 */
	<S> Optional<ServiceReference<S>> serviceRef(Class<S> type, String filter);

	/**
	 * Like {@link #services(Class, String)} with filters but returns the
	 * references.
	 **/
	<S> Collection<ServiceReference<S>> serviceRefs(Class<S> type, String filter);

	/**
	 * Returns the highest-ranked service of the specified type if available. The
	 * service is not explicitly released. It's assumed that the service will be
	 * used until the bundle stops. Doesn't take type parameters into account!
	 *
	 * @tparam S service type
	 * @return service if available
	 */
	<S> Optional<S> service(Class<S> type);

	/**
	 * Returns the first available service of the specified class which satisfies
	 * the filter if available. If the service is not available, it returns `None`.
	 * The service is not explicitly released.
	 *
	 * @param filter
	 *            filter expression
	 * @tparam S service type
	 * @return service if available
	 */
	<S> Optional<S> service(Class<S> type, String filter);

	/**
	 * Returns all services of the specified type which satisfy the given filter.
	 *
	 * @param filter
	 *            filter expression
	 * @tparam S service type
	 * @return services
	 */
	<S> List<S> services(Class<S> type, String filter);

	/**
	 * Returns all services of the given type.
	 *
	 * @tparam S service type
	 */
	<S> List<S> services(Class<S> type);

}
