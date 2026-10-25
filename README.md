# EGit Authenticator Patch

Eclipse plug-in that works around EGit replacing the JVM default
`java.net.Authenticator`, which makes HTTP Basic/Digest authentication fail
(HTTP 401, no login dialog) for other Eclipse features such as
**Javadoc locations on password-protected web servers**.

EGit の `EclipseAuthenticator` がデフォルトの `java.net.Authenticator` を置き換えるため、
Basic認証付きのサイト(Javadocロケーションなど)で認証ダイアログが出ず 401 になる問題を回避する
Eclipse プラグインです。

- Upstream issue: https://github.com/eclipse-egit/egit/issues/221
- Related (historical, no longer accessible): Bugzilla bug 501000

## How it works / 仕組み

On startup the plug-in wraps the current default `Authenticator`.

- Requests from the hosts you configure (Basic/Digest only) are answered with
  your stored credentials.
- Everything else is delegated to the original authenticator
  (EGit's `EclipseAuthenticator`).
- The wrapper is re-installed when EGit/JGit bundles start and every 10 seconds
  if something replaces it.

起動後にデフォルトの Authenticator を包み、設定したホスト(Basic/Digestのみ)には保存した資格情報で応答、
それ以外は元の Authenticator に委譲します。EGit/JGit バンドルの起動時と10秒ごとに、上書きされていないか確認して再設定します。

## Requirements / 動作要件

- Eclipse IDE running on Java 11 or later (tested with Eclipse 2026-03 / EGit 7.6.0 / Temurin 21; older combinations untested)
- Java 9+ API (`Authenticator.getDefault()`) is required, so Java 8 is not supported
- Windows tested; other OSes untested

## Install / インストール

1. Download the jar from [Releases](../../releases) (or build it, see below).
2. Put it into `<eclipse>/dropins/` and restart Eclipse (with `-clean` the first time).

## Configure / 設定

`Window > Preferences > EGit Authenticator Patch`

| Field | Description |
|---|---|
| Hosts | Hosts that require authentication, separated by space/comma. `*.example.co.jp` suffix match is supported. |
| User / Password | Stored in Eclipse Secure Storage. Leave Password empty to keep the existing one. |

**Important:** list only hosts that are *not* Git servers accessed over HTTP.
If the same host serves Git over HTTP, this plug-in may interfere with
EGit/JGit's own credential handling.
Git サーバー(HTTP)と同じホストは指定しないでください。EGit/JGit の認証処理に干渉する可能性があります。

## Build / ビルド

1. `File > Import > Existing Projects into Workspace`
2. `File > Export > Plug-in Development > Deployable plug-ins and fragments`

## Diagnostics / 診断

An INFO entry `Default Authenticator is ...` is written to the Error Log when the
wrapper is installed. Eclipse hides INFO by default; add
`-Declipse.log.level=INFO` to `eclipse.ini` (after `-vmargs`) to see it.

## Limitations / 制限

- NTLM / Kerberos (Negotiate) are not handled.
- This is a workaround. Remove the plug-in once EGit fixes the underlying behavior.

## License

See `LICENSE`.
