package io.github.milankovitch.egitauthpatch;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.Locale;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.osgi.service.prefs.BackingStoreException;

/** 対象ホストは通常の設定、ユーザー/パスワードは Eclipse Secure Storage に保存する。 */
public final class Settings {

	private static final String KEY_HOSTS = "hosts";
	private static final String SECURE_PATH = "/io/github/milankovitch/egitauthpatch";
	private static final String KEY_USER = "user";
	private static final String KEY_PASSWORD = "password";

	private Settings() {
	}

	public static String getHosts() {
		return InstanceScope.INSTANCE.getNode(Activator.ID).get(KEY_HOSTS, "");
	}

	/** 完全一致、または "*.example.co.jp" 形式のサフィックス一致。 */
	public static boolean matchesHost(String host) {
		if (host == null) {
			return false;
		}
		String h = host.toLowerCase(Locale.ROOT);
		for (String token : getHosts().split("[,;\\s]+")) {
			if (token.isEmpty()) {
				continue;
			}
			String t = token.toLowerCase(Locale.ROOT);
			if (t.startsWith("*.") ? h.endsWith(t.substring(1)) : h.equals(t)) {
				return true;
			}
		}
		return false;
	}

	public static String getUser() {
		try {
			return secureNode().get(KEY_USER, "");
		} catch (StorageException e) {
			Activator.log(IStatus.ERROR, "Cannot read user from secure storage", e);
			return "";
		}
	}

	public static PasswordAuthentication getCredentials() {
		try {
			ISecurePreferences node = secureNode();
			String user = node.get(KEY_USER, null);
			String pass = node.get(KEY_PASSWORD, null);
			if (user == null || user.isEmpty() || pass == null) {
				return null;
			}
			return new PasswordAuthentication(user, pass.toCharArray());
		} catch (StorageException e) {
			Activator.log(IStatus.ERROR, "Cannot read credentials from secure storage", e);
			return null;
		}
	}

	/** password が null の場合は既存のパスワードを変更しない。 */
	public static void save(String hosts, String user, String password) {
		try {
			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.ID);
			prefs.put(KEY_HOSTS, hosts == null ? "" : hosts.trim());
			prefs.flush();

			ISecurePreferences node = secureNode();
			node.put(KEY_USER, user == null ? "" : user, true);
			if (password != null) {
				node.put(KEY_PASSWORD, password, true);
			}
			node.flush();
		} catch (BackingStoreException | StorageException | IOException e) {
			Activator.log(IStatus.ERROR, "Cannot save settings", e);
		}
	}

	private static ISecurePreferences secureNode() {
		return SecurePreferencesFactory.getDefault().node(SECURE_PATH);
	}
}
