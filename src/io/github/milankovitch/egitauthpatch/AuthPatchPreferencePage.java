package io.github.milankovitch.egitauthpatch;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class AuthPatchPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text hosts;
	private Text user;
	private Text password;

	@Override
	public void init(IWorkbench workbench) {
		// nothing
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		c.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label desc = new Label(c, SWT.WRAP);
		desc.setText("Javadoc等で社内サイトにアクセスする際の Basic/Digest 認証情報を指定します。\n"
				+ "ホストはスペース/カンマ区切り。*.example.co.jp 形式も可。");
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		gd.widthHint = 400;
		desc.setLayoutData(gd);

		new Label(c, SWT.NONE).setText("Hosts:");
		hosts = new Text(c, SWT.BORDER);
		hosts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		hosts.setText(Settings.getHosts());

		new Label(c, SWT.NONE).setText("User:");
		user = new Text(c, SWT.BORDER);
		user.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		user.setText(Settings.getUser());

		new Label(c, SWT.NONE).setText("Password:");
		password = new Text(c, SWT.BORDER | SWT.PASSWORD);
		password.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		password.setMessage("(変更する場合のみ入力)");

		return c;
	}

	@Override
	public boolean performOk() {
		String pw = password.getText();
		Settings.save(hosts.getText(), user.getText(), pw.isEmpty() ? null : pw);
		return true;
	}
}
