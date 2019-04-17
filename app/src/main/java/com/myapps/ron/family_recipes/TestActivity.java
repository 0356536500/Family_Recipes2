package com.myapps.ron.family_recipes;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

public class TestActivity extends AppCompatActivity {

    private DownloadManager downloadManager;
    private long lastDownload=-1L;
    private File downloaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);

        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        registerReceiver(onNotificationClick,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

        File filesDir = getExternalFilesDir("apk");
        //File file = new File(filesDir, "test.mp4");
        downloaded = new File(filesDir, "Note.apk");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(onComplete);
        unregisterReceiver(onNotificationClick);
    }

    public void startDownload1(View view) {
        File filesDir = getExternalFilesDir("thumbnails");
        File file = new File(filesDir, "chickenfood1.jpg");
        Log.e(getClass().getSimpleName(), "file path: " + file.getAbsolutePath());
        Log.e(getClass().getSimpleName(), "file exists? " + Boolean.toString(file.exists()));
        Uri uri = FileProvider.getUriForFile(this, getPackageName(), file);
        Log.e(getClass().getSimpleName(), uri.toString());

        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setDataAndType(uri, "image/jpeg");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    public void startDownload(View view) {
        //Uri uri=Uri.parse("http://commonsware.com/misc/test.mp4");
        Uri uri = Uri.parse("https://download.apkpure.com/b/apk/eHl6LmhhbmtzLm5vdGVfNzlfNWNiNmY4MGI?_fn=Tm90ZV92Mi42LjNfYXBrcHVyZS5jb20uYXBr&k=e5089aef3baf4c93a6fa6e3b0ca384545cb8ec5b&as=fa1b98961bd7e10fb8c840548e18b09a5cb649d3&_p=eHl6LmhhbmtzLm5vdGU&c=1%7CTOOLS%7CZGV2PWhhbmtzJnQ9YXBrJnZuPTIuNi4zJnZjPTc5");

        File filesDir = getExternalFilesDir("apk");
        //File file = new File(filesDir, "test.mp4");
        downloaded = new File(filesDir, "Note.apk");

        lastDownload =
                downloadManager.enqueue(new DownloadManager.Request(uri)
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false)
                        .setTitle("Demo")
                        .setDescription("Something useful. No, really.")
                        .setDestinationUri(Uri.fromFile(downloaded))
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                );

        view.setEnabled(false);
        findViewById(R.id.query).setEnabled(true);
    }

    public void queryStatus(View view) {
        Cursor c = downloadManager.query(new DownloadManager.Query().setFilterById(lastDownload));

        if (c==null) {
            Toast.makeText(this, "Download not found!", Toast.LENGTH_LONG).show();
        }
        else {
            c.moveToFirst();

            Log.d(getClass().getName(), "COLUMN_ID: "+
                    c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)));
            Log.d(getClass().getName(), "COLUMN_BYTES_DOWNLOADED_SO_FAR: "+
                    c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));
            Log.d(getClass().getName(), "COLUMN_LAST_MODIFIED_TIMESTAMP: "+
                    c.getLong(c.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)));
            Log.d(getClass().getName(), "COLUMN_LOCAL_URI: "+
                    c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
            Log.d(getClass().getName(), "COLUMN_STATUS: "+
                    c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)));
            Log.d(getClass().getName(), "COLUMN_REASON: "+
                    c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));

            Toast.makeText(this, statusMessage(c), Toast.LENGTH_LONG).show();
        }
    }

    public void viewLog(View view) {
        startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
    }

    private String statusMessage(Cursor c) {
        String msg;

        switch(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            case DownloadManager.STATUS_FAILED:
                msg="Download failed!";
                break;

            case DownloadManager.STATUS_PAUSED:
                msg="Download paused!";
                break;

            case DownloadManager.STATUS_PENDING:
                msg="Download pending!";
                break;

            case DownloadManager.STATUS_RUNNING:
                msg="Download in progress!";
                break;

            case DownloadManager.STATUS_SUCCESSFUL:
                msg="Download complete!";
                break;

            default:
                msg="Download is nowhere in sight";
                break;
        }

        return(msg);
    }

    public void installApp(View view) {
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.addCategory("android.intent.category.DEFAULT");
        installIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.setDataAndType(FileProvider.getUriForFile(TestActivity.this, getPackageName(), downloaded), "application/vnd.android.package-archive");
        //installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(installIntent);
    }


    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            findViewById(R.id.start).setEnabled(true);
            Toast.makeText(context, "Finished", Toast.LENGTH_LONG).show();
            installApp(null);
        }
    };

    BroadcastReceiver onNotificationClick=new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Ummmm...hi!", Toast.LENGTH_LONG).show();
        }
    };

}
