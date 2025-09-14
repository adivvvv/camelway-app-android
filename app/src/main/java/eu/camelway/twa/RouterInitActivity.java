// app/src/main/java/eu/camelway/twa/RouterInitActivity.java
package eu.camelway.twa;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.os.LocaleListCompat;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class RouterInitActivity extends Activity {
  private static final int REQ_NOTIF = 1001;

  private Uri launchUri;
  private InstallReferrerClient referrerClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Resolve locale: app-level override -> device -> default.
    LocaleListCompat appLocales = AppCompatDelegate.getApplicationLocales();
    Locale locale = !appLocales.isEmpty()
        ? appLocales.get(0)
        : (getResources().getConfiguration().getLocales() != null
            && !getResources().getConfiguration().getLocales().isEmpty())
          ? getResources().getConfiguration().getLocales().get(0)
          : Locale.getDefault();

    String langTag = locale.toLanguageTag(); // e.g., "pl-PL"
    String country = locale.getCountry();    // e.g., "PL"

    launchUri = Uri.parse("https://camelway.eu/pages/app-router?utm_source=android_app")
        .buildUpon()
        .appendQueryParameter("al", langTag)
        .appendQueryParameter("cc", country)
        .build();

    // On Android 13+ ask OS-level notification permission, once.
    if (shouldAskAndroidNotifPermission()) {
      try {
        Intent req = new Intent(this, com.google.androidbrowserhelper.trusted.NotificationPermissionRequestActivity.class);
        startActivityForResult(req, REQ_NOTIF);
        return;
      } catch (Throwable t) {
        ActivityCompat.requestPermissions(
            this,
            new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
            REQ_NOTIF
        );
        return;
      }
    }

    // Grab Play Install Referrer (UTMs) and then start TWA.
    fetchReferrerThenStart();
  }

  private boolean shouldAskAndroidNotifPermission() {
    if (Build.VERSION.SDK_INT < 33) return false;
    return !NotificationManagerCompat.from(this).areNotificationsEnabled();
  }

  private void fetchReferrerThenStart() {
    try {
      referrerClient = InstallReferrerClient.newBuilder(this).build();
      referrerClient.startConnection(new InstallReferrerStateListener() {
        @Override public void onInstallReferrerSetupFinished(int responseCode) {
          try {
            if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
              ReferrerDetails details = referrerClient.getInstallReferrer();
              String ref = details != null ? details.getInstallReferrer() : null;
              if (ref != null && !ref.isEmpty()) {
                String b64 = base64Url(ref.getBytes(StandardCharsets.UTF_8));
                launchUri = launchUri.buildUpon().appendQueryParameter("gpr", b64).build();
              }
            }
          } catch (Throwable ignored) {
          } finally {
            try { referrerClient.endConnection(); } catch (Throwable ignored) {}
            startTwa();
          }
        }
        @Override public void onInstallReferrerServiceDisconnected() {
          startTwa(); // fallback
        }
      });
    } catch (Throwable t) {
      startTwa(); // fallback
    }
  }

  private static String base64Url(byte[] data) {
    // URL-safe, no padding/wrap: matches your router’s decoder.
    return Base64.encodeToString(data, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
  }

  private void startTwa() {
    Intent twa = new Intent(this, LauncherActivity.class)
        .putExtra("android.support.customtabs.extra.LAUNCH_AS_TRUSTED_WEB_ACTIVITY", true)
        .setData(launchUri);
    startActivity(twa);
    finish();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQ_NOTIF) {
      fetchReferrerThenStart();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQ_NOTIF) {
      fetchReferrerThenStart();
    }
  }
}