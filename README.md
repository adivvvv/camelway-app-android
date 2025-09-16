# CamelWay Android (TWA)

An Android app that opens the CamelWay storefront (**https://camelway.eu**) in a
Trusted Web Activity (TWA). A TWA lets us run Chrome full-screen inside the app
(no URL bar), with the performance and capabilities of the browser, while
verifying the app ↔ site ownership via Digital Asset Links. :contentReference[oaicite:0]{index=0}

---

## What this app does

- **Opens CamelWay in a TWA** with verified App Links for `camelway.eu`.
- **Locale & country aware routing** via `/pages/app-router`:
  - passes OS/app language and country (`al`, `cc`) and Play Install Referrer (`gpr`) to the site.
- **Install referrer support** to preserve UTM tags across first launch.
- **Notification permission prompt** on Android 13+ (once), then defers to site.
- **Lightweight native shell** only; all commerce and account features are handled
  by the web app on `camelway.eu`.

---

## Project structure

app/
src/main/java/eu/camelway/twa/RouterInitActivity.java # builds the launch URI (al/cc/gpr)
src/main/AndroidManifest.xml # TWA launcher + asset links
build.gradle # compile/target SDK, signing, resources


Key resource values are generated in `app/build.gradle` (e.g., `hostName`,
`launchUrl`, theme colors, etc.).

---

## Build & run

**Requirements**

- Android Studio (Ladybug+), AGP 8.9.x, Gradle 8.11.x
- JDK 21+ (Android Studio’s bundled JBR works)
- `compileSdk=36`, `minSdk=21`

**Debug (no signing needed)**

```bash
./gradlew :app:installDebug
```

Release (signed .aab)

Place your upload keystore at the repo root as android.keystore.

Provide credentials via env vars or ~/.gradle/gradle.properties:

CW_STORE_PW=********
CW_KEY_PW=********

---

# Build:

```bash
./gradlew clean :app:bundleRelease
```
Output: app/build/outputs/bundle/release/app-release.aab

---

# Data & privacy

This native shell does not collect or store user data locally. All shopping,
accounts, and analytics are handled by the web application on
[https://camelway.eu]
and its providers (e.g., Shopify/GA/Meta tags configured
on the site).

Privacy Policy: [https://camelway.eu/policies/privacy-policy]
Account deletion request page (if enabled): [https://camelway.eu/pages/delete-account]

Please review and keep the Google Play Data safety form in sync with the site’s
integrations.

---

# Support

For issues specific to the Android shell (build/signing/routing), open a GitHub
issue in this repo. For order/account questions, contact CamelWay support via
the website.

Company: CamelWay Sp. z o.o.
Website: [CamelWay: Camel Milk](https://camelway.eu/)
Author / Maintainer: adivv@adivv.pl

---

# License

This is a private, proprietary project. All rights reserved.
See [LICENSE](LICENSE) for details.
All rights reserved 2025 for CamelWay Camel Milk company. 

