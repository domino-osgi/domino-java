package domino.java;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;
import static de.tobiasroeser.lambdatest.Expect.expectNotEquals;
import static de.tobiasroeser.lambdatest.Expect.expectTrue;
import static de.tobiasroeser.lambdatest.Expect.expectNotNull;

import java.util.Map;

import org.osgi.framework.ServiceReference;

import de.tobiasroeser.lambdatest.junit.FreeSpec;
import domino.java.test.FelixConnectHelper;

public class ServiceProvidingTest extends FreeSpec {

	public interface MyService {
		void doIt();
	}

	public interface MyService2 {
	}

	public static class CombinedService implements MyService, MyService2 {
		public void doIt() {
		}
	}

	public static class ExampleServce implements MyService, MyService2 {
		@Override
		public void doIt() {
		}
	}

	public ServiceProvidingTest() {

		final ExampleServce exampleService = new ExampleServce();

		final CombinedService combinedService = new CombinedService();

		final Map<String, Object> serviceProps = Util.asMap("prop1", "value1", "prop2", 3);

		section("Service providing", () -> {

			test("allow specifying just one interface", () -> {
				FelixConnectHelper.withPojoSr(sr -> {
					class Activator extends OsgiContext {
						public Activator() {
							whenBundleActive(bc -> {
								providesService(combinedService, CombinedService.class);
							});
						}
					}
					final Activator activator = new Activator();
					activator.start(sr.getBundleContext());
					final ServiceReference<?> ref = sr.getServiceReference(CombinedService.class.getName());
					expectNotEquals(ref, null);
					expectTrue(sr.getService(ref) instanceof CombinedService);
					expectEquals(sr.getServiceReference(MyService.class.getName()), null);
					expectEquals(sr.getServiceReference(MyService2.class.getName()), null);

					activator.stop(sr.getBundleContext());
					expectEquals(sr.getServiceReference(CombinedService.class.getName()), null);
				});
			});

			test("allow specifying just one interface and passing service properties", () -> {
				FelixConnectHelper.withPojoSr(sr -> {
					class Activator extends OsgiContext {
						public Activator() {
							whenBundleActive(bc -> {
								providesService(exampleService, MyService.class,
										Util.asMap("prop1", "value1", "prop2", 3));
							});
						}
					}
					final Activator activator = new Activator();
					activator.start(sr.getBundleContext());
					final ServiceReference<?> ref = sr.getServiceReference(MyService.class.getName());
					expectNotEquals(ref, null);
					expectTrue(sr.getService(ref) instanceof MyService);
					expectEquals(ref.getProperty("prop1"), "value1");
					expectEquals(ref.getProperty("prop2"), 3);
					expectEquals(sr.getServiceReference(MyService2.class.getName()), null);
					expectNotEquals(sr.getServiceReferences(MyService.class.getName(), "(&(prop1=value1)(prop2=3))"),
							null);

					activator.stop(sr.getBundleContext());
					expectEquals(sr.getServiceReference(MyService.class.getName()), null);

				});
			});

			test("Mutltiple whenBundleActive via inheritance", () -> {
				FelixConnectHelper.withPojoSr(sr -> {
					class Parent extends OsgiContext {
						public Parent() {
							whenBundleActive(bc -> {
								providesService(exampleService, MyService.class);
							});
						}
					}
					class Child extends Parent {
						public Child() {
							whenBundleActive(bc -> {
								providesService(exampleService, MyService2.class);
							});
						}
					}

					final Child child = new Child();
					child.start(sr.getBundleContext());
					final ServiceReference ref1 = sr.getServiceReference(MyService.class.getName());
					expectNotNull(ref1);

					final ServiceReference ref2 = sr.getServiceReference(MyService2.class.getName());
					expectNotNull(ref2);

					expectNotEquals(ref1, ref2);

				});
			});

		});

	}

}
