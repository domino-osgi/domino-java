package domino.java;

import static de.tototec.utils.functional.FList.append;
import static de.tototec.utils.functional.FList.filter;
import static de.tototec.utils.functional.FList.foreach;
import static de.tototec.utils.functional.FList.headOption;
import static de.tototec.utils.functional.FList.map;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import de.tototec.utils.functional.F1;
import de.tototec.utils.functional.Optional;
import de.tototec.utils.functional.Procedure1;
import de.tototec.utils.functional.Procedure2;
import de.tototec.utils.functional.Procedure3;
import de.tototec.utils.functional.Procedure4;
import domino.java.capsule.Capsule;
import domino.java.capsule.CapsuleScope;
import domino.java.capsule.DynamicCapsuleContext;
import domino.java.internal.Logger;
import domino.java.internal.LoggerFactory;

/**
 * This is the main entry point to the Domino Java DSL.
 * <p>
 * By having your bundle activator extend from this class, you get *full* access
 * to the Domino Java DSL.
 * <p>
 * By calling {@link #whenBundleActive(Procedure1)}, you implicitly create a
 * {@link Capsule} which is tighly coupled to the bundles actived state. All
 * (most) other methods of this class should be called inside this capsule (in
 * the given procedure).
 * <p>
 * Note that if you use
 * {@link #watchAdvancedServices(Class, String, Procedure1)}, you might
 * additionally want to import the relevant watcher events.
 * <p>
 * == Example 1: Wait for a service
 * <p>
 * <pre>
 * // file: MyService.java
 * package org.example.domino_test_one
 *
 * import org.osgi.service.http.HttpService
 *
 * public class MyService {
 *
 *   private final HttpService httpService;
 *
 *   public MyService(HttpService httpService) {
 *     this.httpService = httpService;
 *   }
 *
 * }
 *  </pre>
 *
 * <p>
 * <pre>
 * // file: Activator.java
 * package org.example.domino_test_one
 *
 * import domino.java.OsgiContext
 * import org.osgi.service.http.HttpService
 *
 * public class Activator extends OsgiContext {
 *
 *   public Activator() {
 *
 *     whenBundleActive(bundleContext -> {
 *       // Make MyService available as long as HttpService is present
 *       whenServicePresent(HttpService.class, httpService -> {
 *         MyService myService = new MyService(httpService);
 *         providesService(myService, MyService.class);
 *       });
 *     });
 *   }
 *
 *  }
 * </pre>
 * <p>
 * TODO: Do not allow per default multiple calls to
 * {@link #whenBundleActive(Procedure1)}, and introduce a new setter to allow
 * overriding {@link #whenBundleActive(Procedure1)}
 */
public class OsgiContext
	extends DynamicCapsuleContext
	implements BundleActivator, ServiceWatching, ServiceProviding, ServiceConsuming {

	private static class ActiveHandler {
		private final Procedure1<BundleContext> start;
		private volatile boolean started;

		public ActiveHandler(final Procedure1<BundleContext> handler) {
			this.start = handler;
			this.started = false;
		}

		public boolean isStarted() {
			return started;
		}

		public void setStarted() {
			this.started = true;
		}
	}

	private final Logger log = LoggerFactory.getLogger(OsgiContext.class);

	/**
	 * Contains the handler that {@link #whenBundleActive(Procedure1)} has been
	 * called with.
	 */
	private List<ActiveHandler> bundleActiveHandler = new LinkedList<>();

	/**
	 * Contains the bundle context as long as the bundle is active.
	 */
	private Optional<BundleContext> bundleContext = Optional.none();

	private List<CapsuleScope> bundleActiveCapsuleScope = new LinkedList<>();

	/**
	 * Will be called by the OSGi framework, if you inherit from this class.
	 * <p>
	 * If you construct this class manually, you have to take care to properly
	 * call the {@link #start(BundleContext)} and {@link #stop(BundleContext)}
	 * methods.
	 */
	public OsgiContext() {
	}

	/**
	 * Returns `true` as long as the bundle is active and it's bundle context is
	 * valid.
	 */
	public boolean isActive() {
		return bundleContext.isDefined();
	}

	/**
	 * Defines a handler `f` to be executed when the bundle becomes active. `f`
	 * is executed as soon as the bundle activator's `start` method is called.
	 * This should be called in the constructor of your activator.
	 * <p>
	 * In `f`, you have the opportunity to add so called capsules, which have
	 * their own `start` and `stop` methods (a kind of mini bundles). Their
	 * `stop` methods will be invoked as soon as the bundle activator's `stop`
	 * method is called. So you have the big chance here to encapsulate start
	 * and stop logic at one place, making the bundle activator less
	 * error-prone, better readable and easier to write.
	 *
	 * @param f Handler
	 */
	public void whenBundleActive(final Procedure1<BundleContext> f) {
		// TODO log the caller here
		if (bundleActiveHandler.isEmpty()) {
			log.debug("Registering whenBundleActive");
		} else {
			log.debug("Registering additional wheBundleActive ({})", bundleActiveHandler.size() + 1);
		}

		bundleActiveHandler.add(new ActiveHandler(f));

		// check if we were already started and apply the handler now
		if (bundleContext.isDefined()) {
			internalStart();
		}
	}

	@Override
	public void start(final BundleContext context) {
		if (bundleContext.isDefined()) {
			log.error("A BundleContext is already defined. The OsgiContext / bundle was started before? Bundle [{}]. Ignoring start request!",
				Util.bundleName(context));
			return;
		}

		// Make bundle context available in this class
		bundleContext = Optional.of(context);

		internalStart();
	}

	private void internalStart() {
		bundleContext.foreach(bc -> {
			// Execute the handler if one was defined
			final LinkedList<ActiveHandler> copy;
			synchronized (bundleActiveHandler) {
				copy = new LinkedList<>(bundleActiveHandler);
			}
			foreach(copy, handler -> {
				if (!handler.isStarted()) {
					handler.setStarted();
					log.debug("Bundle {}: Starting whenBundleActive", Util.bundleName(bc));
					// Executes f. All capsules added in f are added to a new
					// capsule
					// scope which is returned afterwards.
					try {
						bundleActiveCapsuleScope = append(bundleActiveCapsuleScope,
							executeWithinNewCapsuleScope(() -> handler.start.apply(bc)));
					} catch (final Throwable e) {
						log.debug("Bundle {}: Exception thrown while starting whenBundleActive", Util.bundleName(bc),
							e);
						throw e;
					}
				}
			});
			if (bundleActiveHandler.isEmpty()) {
				log.warn(
					"Bundle {}: Starting a OsgiContext (Activator) without any registered whenBundleActive handler",
					Util.bundleName(bc));
			}
		});
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		// Stop and release all the capsules in the scope
		try {
			foreach(bundleActiveCapsuleScope, scope -> {
				try {
					log.debug("Bundle {}: Stopping whenBundleActive of bundle: {}", Util.bundleName(context));
					scope.stop();
				} catch (final Throwable e) {
					log.debug("Bundle {}: Exception thrown while stopping whenBundleActive", Util.bundleName(context),
						e);
					throw e;
				} finally {
					bundleActiveCapsuleScope = filter(bundleActiveCapsuleScope, s -> s == scope);
				}
			});

		} finally {
			// Release bundle context
			bundleContext = Optional.none();
		}
	}

	/**
	 * Provides convenient `onStop` method which the end user can use for ad-hoc
	 * adding stop logic to the current scope.
	 *
	 * @param f stop logic
	 */
	public void onStop(final Runnable f) {
		// Create a capsule which just contains stop logic
		final Capsule capsule = new Capsule() {

			@Override
			public void start() {
				// nothing to do
			}

			@Override
			public void stop() {
				f.run();
			}
		};

		// Add the capsule to the current scope
		addCapsule(capsule);
	}

	protected <S> ServiceRegistration<S> internalProvideService(final S service,
																final Iterable<Class<? super S>> interfaces, final Map<String, Object> properties) {
		if (bundleContext.isEmpty()) {
			throw new IllegalStateException(
				"Cannot provide service. This API method must be called with an valid bundle context.");
		}
		final Optional<ServiceRegistration<S>> reg = bundleContext.flatMap(bc -> {
			final ServiceProviderCapsule<S> spc = new ServiceProviderCapsule<>(interfaces, properties, bc, service);
			addCapsule(spc);
			return spc.serviceRegistration();
		});
		return reg.get();
	}

	//////////////////////////////////
	// Service Providing

	@Override
	public <S> ServiceRegistration<S> providesService(final S service) {
		@SuppressWarnings("unchecked") final List<Class<? super S>> types = Arrays.asList((Class<? super S>) service.getClass());
		return internalProvideService(service, types, Collections.emptyMap());
	}

	@Override
	public <S> ServiceRegistration<S> providesService(final S service, final Class<? super S> type) {
		return internalProvideService(service, Arrays.asList(type), Collections.emptyMap());
	}

	@Override
	public <S> ServiceRegistration<S> providesService(final S service, final Class<? super S> type1,
													  final Class<? super S> type2) {
		return internalProvideService(service, Arrays.asList(type1, type2), Collections.emptyMap());
	}

	@Override
	public <S> ServiceRegistration<S> providesService(final S service, final Class<? super S> type1,
													  final Class<? super S> type2, final Class<? super S> type3) {
		return internalProvideService(service, Arrays.asList(type1, type2, type3), Collections.emptyMap());
	}

	@Override
	public <S> ServiceRegistration<S> providesService(final S service, final Class<? super S> type1,
													  final Class<? super S> type2, final Class<? super S> type3, final Class<? super S> type4) {
		return internalProvideService(service, Arrays.asList(type1, type2, type3, type4), Collections.emptyMap());
	}

	@Override
	public <S> ServiceRegistration<S> providesService(final S service, final Map<String, Object> properties) {
		@SuppressWarnings("unchecked") final List<Class<? super S>> types = Arrays.asList((Class<? super S>) service.getClass());
		return internalProvideService(service, types, properties);
	}

	@Override
	public <S> ServiceRegistration<S> providesService(final S service, final Class<? super S> type,
													  final Map<String, Object> properties) {
		return internalProvideService(service, Arrays.asList(type), properties);
	}

	@Override
	public <S> ServiceRegistration<S> providesService(final S service, final Class<? super S> type1,
													  final Class<? super S> type2, final Map<String, Object> properties) {
		return internalProvideService(service, Arrays.asList(type1, type2), properties);
	}

	@Override
	public <S> ServiceRegistration<S> providesService(final S service, final Class<? super S> type1,
													  final Class<? super S> type2, final Class<? super S> type3, final Map<String, Object> properties) {
		return internalProvideService(service, Arrays.asList(type1, type2, type3), properties);
	}

	@Override
	public <S> ServiceRegistration<S> providesService(final S service, final Class<? super S> type1,
													  final Class<? super S> type2, final Class<? super S> type3, final Class<? super S> type4,
													  final Map<String, Object> properties) {
		return internalProvideService(service, Arrays.asList(type1, type2, type3, type4), properties);
	}

	//////////////////////////////////
	// Service Watching

	@Override
	public <S> ServiceTracker<S, S> watchAdvancedServices(final Class<S> type, final String filter,
														  final Procedure1<ServiceWatcherEvent<S>> f) {
		if (bundleContext.isEmpty()) {
			throw new IllegalStateException(
				"Cannot watch service. This API method must be called with an valid bundle context.");
		}
		final Optional<ServiceTracker<S, S>> tracker = bundleContext.flatMap(bc -> {
			final String combinedFilter = Util.createCompleteFilter(type, filter);
			Filter typedFilter;
			try {
				typedFilter = bc.createFilter(combinedFilter);
			} catch (final InvalidSyntaxException e) {
				throw new RuntimeException(
					"Could not create valid filter from generated filter string: " + combinedFilter, e);
			}
			final ServiceWatcherCapsule<S> swc = new ServiceWatcherCapsule<S>(typedFilter, f, bc);
			addCapsule(swc);
			return swc.tracker();
		});
		return tracker.get();
	}

	@Override
	public <S> ServiceTracker<S, S> whenAdvancedServicePresent(final Class<S> type, final String filter,
															   final Procedure1<S> f) {

		class ActivationState {

			private final S watchedService;
			private final CapsuleScope servicePresentCapsuleScope;

			public ActivationState(final S watchedService, final CapsuleScope servicePresentCapsuleScope) {
				this.watchedService = watchedService;
				this.servicePresentCapsuleScope = servicePresentCapsuleScope;
			}

			public CapsuleScope servicePresentCapsuleScope() {
				return servicePresentCapsuleScope;
			}

			public S watchedService() {
				return watchedService;
			}

			@Override
			public String toString() {
				return getClass().getName() + "(watchedService=" + watchedService + ",servicePresentCapsuleScope="
					+ servicePresentCapsuleScope + ")";
			}

		}

		@SuppressWarnings("unchecked") final Optional<ActivationState>[] optActivationState = new Optional[]{Optional.none()};

		final ServiceTracker<S, S> reg = watchAdvancedServices(type, filter, event -> {
			if (event.eventType() == ServiceWatcherEvent.EventType.ADDING) {
				if (optActivationState[0].isEmpty()) {
					// Not already watching a service of this type. Run handler.
					final CapsuleScope newScope = executeWithinNewCapsuleScope(() -> {
						f.apply(event.service());
					});

					// Save the activation state
					optActivationState[0] = Optional.some(new ActivationState(event.service(), newScope));
				}
			} else if (event.eventType() == ServiceWatcherEvent.EventType.REMOVED) {
				optActivationState[0].foreach(activationState -> {
					// Stop the capsule scope only if exactly that service got
					// removed which triggered its creation
					if (event.service() == activationState.watchedService()) {
						activationState.servicePresentCapsuleScope().stop();
						optActivationState[0] = Optional.none();
					}
				});

			}
		});

		return reg;
	}

	@Override
	public <S> ServiceTracker<S, S> whenServicePresent(final Class<S> type, final Procedure1<S> f) {
		return whenAdvancedServicePresent(type, null, f);
	}

	@Override
	public <S1, S2> ServiceTracker<S1, S1> whenServicesPresent(final Class<S1> type1, final Class<S2> type2,
															   final Procedure2<S1, S2> f) {
		return whenServicePresent(type1, (final S1 s1) -> {
			whenServicePresent(type2, (final S2 s2) -> {
				f.apply(s1, s2);
			});
		});
	}

	public <S1, S2, S3> ServiceTracker<S1, S1> whenServicesPresent(final Class<S1> type1, final Class<S2> type2,
																   final Class<S3> type3, final Procedure3<S1, S2, S3> f) {
		return whenServicesPresent(type1, type2, (final S1 s1, final S2 s2) -> {
			whenServicePresent(type3, (final S3 s3) -> {
				f.apply(s1, s2, s3);
			});
		});
	}

	public <S1, S2, S3, S4> ServiceTracker<S1, S1> whenServicesPresent(
		final Class<S1> type1,
		final Class<S2> type2,
		final Class<S3> type3,
		final Class<S4> type4,
		final Procedure4<S1, S2, S3, S4> f) {
		return whenServicesPresent(type1, type2, type3, (final S1 s1, final S2 s2,
														 final S3 s3) -> {
			whenServicePresent(type4, (final S4 s4) -> {
				f.apply(s1, s2, s3, s4);
			});
		});
	}

	/////////////////////////////////
	// Service Consuming

	@Override
	public <S, R> R withService(final Class<S> type, final F1<Optional<S>, R> f) {
		if (bundleContext.isEmpty()) {
			throw new IllegalStateException(
				"Cannot get service. This API method must be called with an valid bundle context.");
		}

		final BundleContext bc = bundleContext.get();
		final Optional<ServiceReference<S>> ref = serviceRef(type);
		if (ref.isDefined()) {
			final S s = bc.getService(ref.get());
			try {
				return f.apply(Optional.some(s));
			} finally {
				bc.ungetService(ref.get());
			}
		} else {
			return f.apply(Optional.none());
		}
	}

	@Override
	public <S> Optional<ServiceReference<S>> serviceRef(final Class<? super S> type) {
		if (bundleContext.isEmpty()) {
			throw new IllegalStateException(
				"Cannot get service reference. This API method must be called with an valid bundle context.");
		}
		final ServiceReference<?> ref = bundleContext.get().getServiceReference(type.getName());
		return Optional.of(ref).map(r -> {
			@SuppressWarnings("unchecked") final ServiceReference<S> s = (ServiceReference<S>) r;
			return s;
		});
	}

	@Override
	public <S> Optional<ServiceReference<S>> serviceRef(final Class<S> type, final String filter) {
		final Collection<ServiceReference<S>> refs = serviceRefs(type, filter);
		return headOption(refs);
	}

	@Override
	public <S> Collection<ServiceReference<S>> serviceRefs(final Class<S> type, final String filter) {
		if (bundleContext.isEmpty()) {
			throw new IllegalStateException(
				"Cannot get service references. This API method must be called with an valid bundle context.");
		}
		final BundleContext bc = bundleContext.get();

		// Get the list of references matching the filter
		Collection<ServiceReference<S>> refs = null;
		try {
			refs = bc.getServiceReferences(type, filter);
		} catch (final InvalidSyntaxException e) {
			throw new RuntimeException("Invalid filter syntax: " + filter, e);
		}

		if (refs == null) {
			return Collections.emptyList();
		} else {
			return refs;
		}
	}

	@Override
	public <S> Optional<S> service(final Class<S> type) {
		if (bundleContext.isEmpty()) {
			throw new IllegalStateException(
				"Cannot get service. This API method must be called with an valid bundle context.");
		}
		return serviceRef(type).map(s -> bundleContext.get().getService(s));
	}

	@Override
	public <S> Optional<S> service(final Class<S> type, final String filter) {
		if (bundleContext.isEmpty()) {
			throw new IllegalStateException(
				"Cannot get service. This API method must be called with an valid bundle context.");
		}
		return serviceRef(type, filter).map(s -> bundleContext.get().getService(s));
	}

	@Override
	public <S> List<S> services(final Class<S> type, final String filter) {
		if (bundleContext.isEmpty()) {
			throw new IllegalStateException(
				"Cannot get services. This API method must be called with an valid bundle context.");
		}
		final BundleContext bc = bundleContext.get();
		final Collection<ServiceReference<S>> refs = serviceRefs(type, filter);
		return map(refs, ref -> bc.getService(ref));
	}

	@Override
	public <S> List<S> services(final Class<S> type) {
		return services(type, null);
	}
}
