package io.github.milankovitch.egitauthpatch;

import org.eclipse.ui.IStartup;

/** ワークベンチ起動後に呼ばれ、Authenticator の差し込みと監視を開始する。 */
public class AuthPatchStartup implements IStartup {

	@Override
	public void earlyStartup() {
		AuthenticatorInstaller.start();
	}
}
