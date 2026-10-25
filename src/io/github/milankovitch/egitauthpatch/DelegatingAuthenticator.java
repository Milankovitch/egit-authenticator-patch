package io.github.milankovitch.egitauthpatch;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.eclipse.core.runtime.IStatus;

/**
 * 社内サイト(設定したホスト)への Basic/Digest 認証要求には自前で応答し、
 * それ以外は元の(EGit等が設定した)Authenticator に委譲する。
 */
public class DelegatingAuthenticator extends Authenticator {

	private final Authenticator delegate;

	public DelegatingAuthenticator(Authenticator delegate) {
		this.delegate = delegate;
	}

	public Authenticator getDelegate() {
		return delegate;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		if (getRequestorType() == RequestorType.SERVER
				&& isSupportedScheme(getRequestingScheme())
				&& Settings.matchesHost(getRequestingHost())) {
			PasswordAuthentication pa = Settings.getCredentials();
			if (pa != null) {
				return pa;
			}
		}
		if (delegate == null) {
			return null;
		}
		try {
			// Java 9+: 指定した Authenticator に要求を転送できる
			return Authenticator.requestPasswordAuthentication(delegate,
					getRequestingHost(), getRequestingSite(),
					getRequestingPort(), getRequestingProtocol(),
					getRequestingPrompt(), getRequestingScheme(),
					getRequestingURL(), getRequestorType());
		} catch (RuntimeException e) {
			// 委譲先が例外を投げても呼び出し元(Javadoc取得等)を巻き込まない
			Activator.log(IStatus.WARNING,
					"Delegate authenticator failed: " + delegate.getClass().getName(), e);
			return null;
		}
	}

	private static boolean isSupportedScheme(String scheme) {
		return "basic".equalsIgnoreCase(scheme) || "digest".equalsIgnoreCase(scheme);
	}
}
