package io.github.milankovitch.egitauthpatch;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static final String ID = "io.github.milankovitch.egitauthpatch";

	private static Activator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		AuthenticatorInstaller.stop();
		plugin = null;
		super.stop(context);
	}

	public static void log(int severity, String message, Throwable t) {
		Activator a = plugin;
		if (a != null) {
			a.getLog().log(new Status(severity, ID, message, t));
		}
	}

	public static void info(String message) {
		log(IStatus.INFO, message, null);
	}
}
