package io.github.milankovitch.egitauthpatch;

import java.net.Authenticator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkUtil;

/**
 * デフォルト Authenticator を DelegatingAuthenticator で包む。
 * EGit/JGit のバンドル起動時、および一定間隔で、上書きされていないか確認して再設定する。
 */
public final class AuthenticatorInstaller {

	private static final long CHECK_INTERVAL_MS = 10_000;

	private static BundleListener listener;
	private static Job watchdog;

	private AuthenticatorInstaller() {
	}

	public static synchronized void start() {
		ensureInstalled();

		BundleContext ctx = FrameworkUtil.getBundle(AuthenticatorInstaller.class).getBundleContext();
		listener = event -> {
			if (event.getType() == BundleEvent.STARTED) {
				String n = event.getBundle().getSymbolicName();
				if (n != null && (n.startsWith("org.eclipse.egit") || n.startsWith("org.eclipse.jgit"))) {
					ensureInstalled();
				}
			}
		};
		ctx.addBundleListener(listener);

		watchdog = new Job("Authenticator watchdog") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				ensureInstalled();
				schedule(CHECK_INTERVAL_MS);
				return Status.OK_STATUS;
			}
		};
		watchdog.setSystem(true);
		watchdog.schedule(CHECK_INTERVAL_MS);
	}

	public static synchronized void stop() {
		if (watchdog != null) {
			watchdog.cancel();
			watchdog = null;
		}
		if (listener != null) {
			try {
				FrameworkUtil.getBundle(AuthenticatorInstaller.class).getBundleContext()
						.removeBundleListener(listener);
			} catch (IllegalStateException e) {
				// bundle context already invalid
			}
			listener = null;
		}
		Authenticator current = Authenticator.getDefault();
		if (current instanceof DelegatingAuthenticator) {
			Authenticator.setDefault(((DelegatingAuthenticator) current).getDelegate());
		}
	}

	public static void ensureInstalled() {
		synchronized (AuthenticatorInstaller.class) {
			Authenticator current = Authenticator.getDefault();
			if (current instanceof DelegatingAuthenticator) {
				return;
			}
			// 診断用: 誰がデフォルトを持っているか(EGit のクラス名が出るはず)
			Activator.info("Default Authenticator is "
					+ (current == null ? "null" : current.getClass().getName())
					+ "; installing delegating authenticator");
			Authenticator.setDefault(new DelegatingAuthenticator(current));
		}
	}
}
