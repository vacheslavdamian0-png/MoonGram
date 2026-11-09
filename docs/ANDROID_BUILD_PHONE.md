# Build MoonGram APK from an Android phone

The repository includes a GitHub Actions workflow at `.github/workflows/moongram-android-apk.yml`.
It builds a debug APK in GitHub's cloud runner; Android Studio or a PC is not required.

## One-time setup

1. Open the repository on GitHub.
2. Go to **Settings → Secrets and variables → Actions**.
3. Add two **Repository secrets**:
   - `MOONGRAM_API_ID`: your own numeric Telegram API ID.
   - `MOONGRAM_API_HASH`: your own 32-character Telegram API hash.
4. Do not paste these values into source files, issues, pull requests, or chat.

The build intentionally stops if either secret is missing or malformed. The app reads these values from Gradle-generated BuildConfig fields; the real credentials are not committed to Git.

## Start a build on Android

1. Open the repository in Chrome or the GitHub app.
2. Select **Actions**.
3. Open **MoonGram Android APK**.
4. Tap **Run workflow**, choose `feature/moongram-miniapp-admin`, then confirm.
5. Open the new workflow run and wait for the green check mark.
6. Under **Artifacts**, download `MoonGram-debug-APK`.
7. Extract the ZIP and tap `app.apk` to install it.

## Important limitations

- This is a debug/test APK, not a signed release build for RuStore.
- The current application ID and Firebase configuration are still inherited from the upstream Telegram build. Before public distribution, create a unique MoonGram application ID and your own Firebase Android app/configuration.
- A separate release signing key must be created and kept private. Never commit a keystore or passwords.
- The local preset banner selection is not yet synchronized between accounts or rendered in other users' profiles.
- Ghost privacy automation and public role-badge rendering in the native profile are not yet complete.
- Do not publish the app until the Telegram API credentials, package ID, Firebase configuration, signing key, licensing notices, and privacy behavior have been reviewed.
