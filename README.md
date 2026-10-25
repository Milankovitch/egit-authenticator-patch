# EGit Authenticator Patch

[English](#english) | [日本語](#日本語)

---

## English

An Eclipse plug-in that works around EGit replacing the JVM default
`java.net.Authenticator`.

With EGit installed, HTTP Basic/Digest authentication requested by other Eclipse
features does not show the usual login dialog and fails with HTTP 401. A typical
example is a **Javadoc location on a password-protected web server**.

- Upstream issue: https://github.com/eclipse-egit/egit/issues/221
- Related (historical, no longer accessible): Bugzilla bug 501000

### How it works

On startup the plug-in wraps the current default `Authenticator`.

- Requests from the hosts you configure (Basic/Digest only) are answered with
  the credentials you stored.
- Everything else is delegated to the original authenticator
  (EGit's `EclipseAuthenticator`).
- The wrapper is re-installed when EGit/JGit bundles start, and checked every
  10 seconds in case something replaces it again.

### Requirements

- Eclipse IDE running on Java 11 or later
- Tested with: Eclipse 2026-03, EGit 7.6.0, Temurin 21, Windows
- Older Eclipse/EGit versions and other operating systems are untested
- Java 8 is not supported (`Authenticator.getDefault()` requires Java 9+)

### Install

1. Download the jar from [Releases](https://github.com/Milankovitch/egit-authenticator-patch/releases).
2. Put it into `<eclipse>/dropins/`.
3. Restart Eclipse (use `-clean` the first time).

To update, delete the old jar from `dropins` first, then put the new one.
To uninstall, delete the jar and restart Eclipse.

### Configure

Open `Window > Preferences > EGit Authenticator Patch`.

| Field | Description |
|---|---|
| Hosts | Hosts that require authentication, separated by spaces or commas. A suffix pattern such as `*.example.co.jp` is also supported. |
| User | User name. Stored in Eclipse Secure Storage. |
| Password | Password. Stored in Eclipse Secure Storage. Leave it empty to keep the stored one. |

**Important:** list only hosts that do **not** serve Git over HTTP.
If the same host serves Git repositories, this plug-in may interfere with the
credential handling of EGit/JGit.

### Troubleshooting

When the wrapper is installed, an INFO entry like
`Default Authenticator is ...` is written to the Error Log. It shows which class
held the default `Authenticator` before the plug-in (for example,
`org.eclipse.egit.core.EclipseAuthenticator`).

Eclipse hides INFO entries by default. To see them, add the following to
`eclipse.ini` after `-vmargs`, then restart:

```
-Declipse.log.level=INFO
```

If it does not work in your environment, please open an issue with the Error Log
entry (mask internal host names).

### Build

1. `File > Import > Existing Projects into Workspace`
2. `File > Export > Plug-in Development > Deployable plug-ins and fragments`
   (enable "Package plug-ins as individual JAR archives")

### Limitations

- NTLM and Kerberos (Negotiate) are not handled.
- This is a workaround. Remove the plug-in once EGit fixes the underlying behavior.

---

## 日本語

EGit が JVM のデフォルト `java.net.Authenticator` を置き換えてしまう問題を回避する
Eclipse プラグインです。

EGit をインストールしていると、Eclipse の他の機能が HTTP の Basic/Digest 認証を
必要としたときに、通常のログインダイアログが表示されず HTTP 401 で失敗します。
代表的な例は、**認証付き Web サーバー上の Javadoc ロケーション**です。

- EGit への報告: https://github.com/eclipse-egit/egit/issues/221
- 関連(過去の報告、現在は閲覧不可): Bugzilla bug 501000

### 仕組み

起動時に、現在のデフォルト `Authenticator` を包みます。

- 設定したホストからの認証要求(Basic/Digest のみ)には、保存した認証情報で応答します。
- それ以外の要求は、元の Authenticator(EGit の `EclipseAuthenticator`)に委譲します。
- EGit/JGit のバンドルが起動したときに再設定し、さらに 10 秒ごとに、再び置き換えられていないか確認します。

### 動作要件

- Java 11 以上で動作する Eclipse IDE
- 動作確認済み: Eclipse 2026-03、EGit 7.6.0、Temurin 21、Windows
- それより古い Eclipse/EGit や、Windows 以外の OS は未確認です
- Java 8 は非対応です(`Authenticator.getDefault()` が Java 9 以降のため)

### インストール

1. [Releases](https://github.com/Milankovitch/egit-authenticator-patch/releases) から jar をダウンロードします。
2. `<eclipse>/dropins/` に置きます。
3. Eclipse を再起動します(初回は `-clean` を付けてください)。

更新するときは、`dropins` の古い jar を先に削除してから、新しい jar を置いてください。
アンインストールするときは、jar を削除して Eclipse を再起動します。

### 設定

`ウィンドウ > 設定 > EGit Authenticator Patch` を開きます。

| 項目 | 説明 |
|---|---|
| Hosts | 認証が必要なホスト。スペースまたはカンマ区切り。`*.example.co.jp` のような末尾一致の指定も可能です。 |
| User | ユーザー名。Eclipse のセキュア・ストレージに保存されます。 |
| Password | パスワード。Eclipse のセキュア・ストレージに保存されます。空のままにすると、保存済みのパスワードを変更しません。 |

**重要:** HTTP で Git を提供しているホストは、**指定しないでください**。
同じホストを指定すると、EGit/JGit の認証処理に干渉する可能性があります。

### トラブルシューティング

差し込みが行われると、エラー・ログに `Default Authenticator is ...` という
INFO エントリーが出力されます。プラグインが差し込む前にデフォルトの `Authenticator`
だったクラス(たとえば `org.eclipse.egit.core.EclipseAuthenticator`)が分かります。

Eclipse は、既定では INFO を表示しません。確認するには、`eclipse.ini` の
`-vmargs` の後ろに次の行を追加して、再起動してください。

```
-Declipse.log.level=INFO
```

環境によって動作しない場合は、エラー・ログの内容(社内のホスト名はマスクしてください)を
添えて、Issue を作成してください。

### ビルド

1. `ファイル > インポート > 既存プロジェクトをワークスペースへ`
2. `ファイル > エクスポート > プラグイン開発 > デプロイ可能なプラグインおよびフラグメント`
   (「プラグインを個別の JAR アーカイブとしてパッケージ」を有効にしてください)

### 制限

- NTLM と Kerberos(Negotiate)は処理しません。
- これは回避策です。EGit 側で根本的に修正されたら、このプラグインを削除してください。

---

## License / ライセンス

See [LICENSE](LICENSE). / [LICENSE](LICENSE) を参照してください。
