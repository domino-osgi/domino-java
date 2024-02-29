package domino.java;

import java.util.Map;

import org.osgi.framework.ServiceRegistration;

/**
 * Provides convenient methods to provide any object easily in the OSGi service
 * registry.
 * 
 */
public interface ServiceProviding {

	/**
	 * Registers the service under it's own specified type and without service
	 * properties.
	 *
	 * @return The service registration.
	 */
	<S> ServiceRegistration<S> providesService(S service);

	/**
	 * Registers the service under the specified type and without service
	 * properties.
	 *
	 * @return The service registration.
	 */
	<S> ServiceRegistration<S> providesService(S service, Class<? super S> type);

	/**
	 * Registers the service under the specified types and without service
	 * properties.
	 *
	 * @return The service registration.
	 */
	<S> ServiceRegistration<S> providesService(S service, Class<? super S> type1, Class<? super S> type2);

	/**
	 * Registers the service under the specified types and without service
	 * properties.
	 *
	 * @see Util#asMap(Object, Object) to easily create a map in-place.
	 * 
	 * @param service
	 *            The service.
	 * @return The service registration.
	 */
	<S> ServiceRegistration<S> providesService(
			S service,
			Class<? super S> type1,
			Class<? super S> type2,
			Class<? super S> type3);

	/**
	 * Registers the service under the specified types and without service
	 * properties.
	 *
	 * @see Util#asMap(Object, Object) to easily create a map in-place.
	 * 
	 * @param service
	 *            The service.
	 * @return The service registration.
	 */
	<S> ServiceRegistration<S> providesService(
			S service,
			Class<? super S> type1,
			Class<? super S> type2,
			Class<? super S> type3,
			Class<? super S> type4);

	/**
	 * Registers the service under it's own specified type with the given service
	 * properties.
	 *
	 * @see Util#asMap(Object, Object) to easily create a map in-place.
	 * 
	 * @param service
	 *            The service.
	 * @param properties
	 *            The service properties.
	 * @return The service registration.
	 */
	<S> ServiceRegistration<S> providesService(S service, Map<String, Object> properties);

	/**
	 * Registers the service under the specified type with the given service
	 * properties.
	 *
	 * @see Util#asMap(Object, Object) to easily create a map in-place.
	 * 
	 * @param service
	 *            The service.
	 * @param properties
	 *            The service properties.
	 * @return The service registration.
	 */
	<S> ServiceRegistration<S> providesService(S service, Class<? super S> type, Map<String, Object> properties);

	/**
	 * Registers the service under the specified types with the given service
	 * properties.
	 *
	 * @see Util#asMap(Object, Object) to easily create a map in-place.
	 * 
	 * @param service
	 *            The service.
	 * @param properties
	 *            The service properties.
	 * @return The service registration.
	 */
	<S> ServiceRegistration<S> providesService(
			S service,
			Class<? super S> type1,
			Class<? super S> type2,
			Map<String, Object> properties);

	/**
	 * Registers the service under the specified types with the given service
	 * properties.
	 *
	 * @see Util#asMap(Object, Object) to easily create a map in-place.
	 * 
	 * @param service
	 *            The service.
	 * @param properties
	 *            The service properties.
	 * @return The service registration.
	 */
	<S> ServiceRegistration<S> providesService(S service,
			Class<? super S> type1,
			Class<? super S> type2,
			Class<? super S> type3,
			Map<String, Object> properties);

	/**
	 * Registers the service under the specified types with the given service
	 * properties.
	 *
	 * @see Util#asMap(Object, Object) to easily create a map in-place.
	 * 
	 * @param service
	 *            The service.
	 * @param properties
	 *            The service properties.
	 * @return The service registration.
	 */
	<S> ServiceRegistration<S> providesService(S service,
			Class<? super S> type1,
			Class<? super S> type2,
			Class<? super S> type3,
			Class<? super S> type4,
			Map<String, Object> properties);
}
