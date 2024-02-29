package domino.java.test;

import java.io.File;

import org.apache.felix.connect.PojoSR;
import org.apache.felix.connect.launch.PojoServiceRegistry;

import de.tobiasroeser.lambdatest.ProcedureWithException;
import domino.java.Util;

public class FelixConnectHelper {

	private static final Object onlyOneFelixConnectAtATime = new Object();

	public static void withPojoSr(ProcedureWithException<PojoServiceRegistry> f) throws Exception {
		synchronized (onlyOneFelixConnectAtATime) {
			File dir = File.createTempFile("pojosr-", "");
			dir.delete();
			dir.mkdirs();
			try {
				System.setProperty("org.osgi.framework.storage", dir.getAbsolutePath());
				PojoSR registry = new PojoSR(Util.asMap("felix.cm.dir", dir.getAbsolutePath()));
				f.apply(registry);
			} finally {
				System.clearProperty("org.osgi.framework.storage");
				deleteRecursive(dir);
			}
		}
	}

	public static void deleteRecursive(File... files) {
		for (File file : files) {
			if (file.isDirectory())
				deleteRecursive(file.listFiles());
			if (!file.delete() && file.exists()) {
				String dirOrFile = file.isDirectory() ? "dir" : "file";
				throw new RuntimeException("Could not delete " + dirOrFile + ": " + file);
			}
		}
	}

}
