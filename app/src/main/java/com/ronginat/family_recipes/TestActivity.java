package com.ronginat.family_recipes;

/*import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.ronginat.family_recipes.utils.logic.CrashLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;*/

public class TestActivity/* extends AppCompatActivity*/ {

    /*private static final String TAG = TestActivity.class.getSimpleName();

    public static final String ACTION_INSTALL_COMPLETE
            = "com.ronginat.family_recipes.INSTALL_COMPLETE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void installPackage(View view) throws IOException {
        Uri installUri = null;//ExternalStorageHelper.getFileUri(this, Constants.APK_DIR, "installMe.apk");
        InputStream in = null;
        if (installUri != null)
            in = getContentResolver().openInputStream(installUri);
        if (in != null) {
            PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.setAppPackageName("com.socialnmobile.dictapps.notepad.color.note");
            int sessionId = packageInstaller.createSession(params);
            PackageInstaller.Session session = packageInstaller.openSession(sessionId);
            OutputStream out = session.openWrite("notes_test", 0 , -1);
            byte[] buffer = new byte[1024 * 64];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            session.fsync(out);
            in.close();
            out.close();

            session.commit(createInstallIntentSender(sessionId));
            registerReceiver(mReceiver, new IntentFilter(ACTION_INSTALL_COMPLETE));
        }
    }

    private IntentSender createInstallIntentSender(int sessionId) {
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, sessionId,
                new Intent(ACTION_INSTALL_COMPLETE), 0);
        return pendingIntent.getIntentSender();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CrashLogger.d(TAG, intent.getAction());
            if (ACTION_INSTALL_COMPLETE.equals(intent.getAction())) {
                int result = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
                CrashLogger.e(TAG, "status = " + result);
                CrashLogger.e(TAG, "message = " + intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
                CrashLogger.e(TAG, "packageName = " + intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME));

                switch (result) {
                    case PackageInstaller.STATUS_PENDING_USER_ACTION: {
                        // this should not happen in M, but will happen in L and L-MR1
                        startActivity(intent.getParcelableExtra(Intent.EXTRA_INTENT));
                    } break;
                    case PackageInstaller.STATUS_SUCCESS: {
                        CrashLogger.e(TAG, "notes installed successfully");
                    } break;
                    default: {
                        CrashLogger.e(TAG, "Install failed.");
                        break;
                    }
                }
            }
        }
    };*/
}
