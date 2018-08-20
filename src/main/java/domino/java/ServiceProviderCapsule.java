package domino.java;

import static de.tototec.utils.functional.FList.map;
import static de.tototec.utils.functional.FList.mkString;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.tototec.utils.functional.Optional;
import domino.java.capsule.Capsule;
import domino.java.internal.Logger;
import domino.java.internal.LoggerFactory;

/**
 * A capsule which registers an object in the OSGi service registry while the
 * current capsule scope is active.
 *
 * @param types
 *            Types under which to register the given service in the OSGi
 *            registry
 * @param properties
 *            Service properties
 * @param bundleContext
 *            Bundle context
 * @param service
 *            The object to be registered
 * @tparam S Service type
 */
public class ServiceProviderCapsule<S> implements Capsule {

	private final Logger log = LoggerFactory.getLogger(ServiceProviderCapsule.class);

	private final Iterable<Class<? super S>> interfaces;
	private final Map<String, Object> properties;
	private final BundleContext bundleContext;
	private final S service;

	public ServiceProviderCapsule(
			final Iterable<Class<? super S>> interfaces,
			final Map<String, Object> properties,
			final BundleContext bundleContext,
			final S service) {
		this.interfaces = interfaces;
		this.properties = properties;
		this.bundleContext = bundleContext;
		this.service = service;

	}

	private Optional<ServiceRegistration<S>> serviceRegistration = Optional.none();

	/**
	 * Returns the service registration.
	 */
	public Optional<ServiceRegistration<S>> serviceRegistration() {
		return serviceRegistration;
	}

	@Override
	public void start() {
		// Create array of class names under which the service shall be
		// registered
		final List<String> types = map(interfaces, i -> i.getName());

		final Hashtable<String, ?> props = new Hashtable<>(properties);

		if (log.isDebugEnabled()) {
			log.debug("Bundle {}: Register service [${service}] with interfaces [{}} and properties [{}]",
					Util.bundleName(bundleContext), service, mkString(types, ", "), properties);
		}

		// Register service
		@SuppressWarnings("unchecked")
		final ServiceRegistration<S> serviceRegistration = (ServiceRegistration<S>) bundleContext
				.registerService(types.toArray(new String[types.size()]), service, props);
		this.serviceRegistration = Optional.of(serviceRegistration);
	}

	@Override
	public void stop() {
		serviceRegistration.foreach(reg -> {

			try {
				if (log.isDebugEnabled()) {
					final List<String> types = map(interfaces, i -> i.getName());
					log.debug("Bundle {}: Unregister service: [{}] with interfaces [{}] and properties [{}]",
							Util.bundleName(bundleContext), service, mkString(types, ", "), properties);
				}
				reg.unregister();
			} catch (final IllegalStateException e) {
				// Do nothing. Was already unregistered.
			}
			serviceRegistration = Optional.none();
		});

	}

}
