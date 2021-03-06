package com.ronginat.family_recipes.background.services;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.ui.activities.SplashActivity;
import com.ronginat.family_recipes.utils.Constants;
import com.ronginat.family_recipes.utils.logic.CrashLogger;
import com.ronginat.family_recipes.utils.logic.SharedPreferencesHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.ronginat.family_recipes.layout.Constants.ID_QUERY;
import static com.ronginat.family_recipes.layout.Constants.LAST_MODIFIED_QUERY;

/**
 * Created by ronginat on 10/02/2019.
 */
public class MyMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyMessagingService.class.getSimpleName();

    @SuppressWarnings("FieldCanBeLocal")
    private final String DEFAULT_GROUP = "com.ronginat.family_recipes.DEFAULT";

    @SuppressWarnings("FieldCanBeLocal")
    private final int SUMMARY_ID = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        //registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(mReceiver);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        CrashLogger.e(TAG, "onMessageReceived");
        CrashLogger.e(TAG, remoteMessage.getData().toString());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            CrashLogger.e(TAG, "Message Notification: " + remoteMessage.getNotification().toString());
            //remoteMessage.getNotification().getClickAction();
        }

        /*if (remoteMessage.getNotification() == null && remoteMessage.getData() != null) { */
            /*
            Instead of sending a “notification” entry in the payload, change it for a “data” entry.
            In this way the notifications will ALWAYS be managed by the app through onMessageReceived
            https://medium.com/@cdmunoz/working-easily-with-fcm-push-notifications-in-android-e1804c80f74
            */
        sendNotification(remoteMessage.getData());
        //}

    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        CrashLogger.e(TAG, "Refreshed token: " + token);
        SharedPreferencesHandler.writeString(getApplicationContext(), Constants.NEW_FIREBASE_TOKEN, token);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param data FCM message data received.
     */
    private void sendNotification(@NonNull Map<String, String> data) {
        CrashLogger.e(TAG, "sendNotification");
        String channelId = data.get(Constants.CHANNEL);
        if (channelId == null)
            channelId = getString(R.string.notification_new_recipe_channel_id);

        int notificationID = new Random(System.currentTimeMillis()).nextInt();
        // create notification click intent
        PendingIntent pendingIntent = getIntentToStartActivity(data, channelId);
        //PendingIntent deletionIntent = getDeletionIntent(notificationID);

        // set the notification UI
        NotificationCompat.Builder notificationBuilder = getNotificationBuilderUI(data, channelId, pendingIntent/*, deletionIntent*/);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannels(notificationManager);
            }

            // send notification to the user
            notificationManager.notify(notificationID /* ID of notification */, notificationBuilder.build());
            notificationManager.notify(SUMMARY_ID, getSummaryBuilderUI().build());
        }
    }

    private PendingIntent getIntentToStartActivity(Map<String, String> data, String channelId) {
        Intent intent;
        if (data.get(ID_QUERY) != null) {
            intent = new Intent(this, SplashActivity.class);
            /*Bundle bundle = new Bundle();
            bundle.putString(Constants.RECIPE_ID, recipeId);
            bundle.putBoolean("notification", true);
            bundle.putString("channel", channelId);
            intent.putExtras(bundle);*/
            intent.putExtra(Constants.SPLASH_ACTIVITY_CODE, Constants.SPLASH_ACTIVITY_CODE_RECIPE);
            intent.putExtra(Constants.RECIPE_ID, data.get(ID_QUERY));
            intent.putExtra(Constants.LAST_MODIFIED, data.get(LAST_MODIFIED_QUERY));
            intent.putExtra(Constants.NOTIFICATION, true);
            intent.putExtra(Constants.CHANNEL, channelId); // whether to fetch new recipes or just load comments normally
        }
        else
            intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        return PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
    }

    /*private PendingIntent getDeletionIntent(int notificationID) {
        Intent intent = new Intent(NOTIFICATION_DELETION_ACTION);
        intent.putExtra(Constants.ID, notificationID);
        return PendingIntent.getBroadcast(this, NOTIFICATION_DELETION_REQUEST, intent, PendingIntent.FLAG_ONE_SHOT);
    }*/

    private NotificationCompat.Builder getNotificationBuilderUI(Map<String, String> data, @NonNull String channelId, PendingIntent pendingIntent/*, PendingIntent deletionIntent*/) {
        // set the notification UI
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_status_logo)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo_foreground))
                        .setContentTitle(buildNotificationTitle(channelId, data))
                        .setContentText(buildNotificationMessage(channelId, data.get(Constants.BODY)))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(buildNotificationMessage(channelId, data.get(Constants.BODY))))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setColorized(true)
                        .setColor(ContextCompat.getColor(this, R.color.logo_foreground))
                        .setGroup(DEFAULT_GROUP)
                        .setContentIntent(pendingIntent);
                        //.setDeleteIntent(deletionIntent);

        if (channelId.equals(getString(R.string.notification_new_recipe_channel_id))) {
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_post_black));
        }
        return notificationBuilder;
    }

    private String buildNotificationTitle(@NonNull String channelId, Map<String, String> data) {
        // new recipe notification
        if (getString(R.string.notification_new_recipe_channel_id).equals(channelId))
            return getString(R.string.notification_new_recipe_channel_title, data.get(Constants.TITLE), data.get(Constants.AUTHOR));
        else if (getString(R.string.notification_comment_channel_id).equals(channelId))
            return getString(R.string.notification_comment_channel_title, data.get(Constants.AUTHOR), data.get(Constants.TITLE));
        else if (getString(R.string.notification_like_channel_id).equals(channelId))
            return getString(R.string.notification_like_channel_title, data.get(Constants.AUTHOR), data.get(Constants.TITLE));
        return data.get(Constants.TITLE);
    }

    private String buildNotificationMessage(@NonNull String channelId, String message) {
        String defaultMessage = getString(R.string.notification_generic_message);
        if (getString(R.string.notification_like_channel_id).equals(channelId))
            return defaultMessage;
        return message != null ? message : defaultMessage;
    }

    private NotificationCompat.Builder getSummaryBuilderUI() {
        return new NotificationCompat.Builder(this, getString(R.string.notification_new_recipe_channel_id))
                .setSmallIcon(R.drawable.ic_status_logo)
                .setAutoCancel(true)
                .setColorized(true)
                .setColor(ContextCompat.getColor(this, R.color.logo_foreground))
                //.setNumber(messageCount)
                .setGroup(DEFAULT_GROUP)
                .setGroupSummary(true);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannels(NotificationManager notificationManager) {
        List<NotificationChannel> channels = new ArrayList<>();
        //createNotificationChannelGroup(notificationManager);
        channels.add(new NotificationChannel(getString(R.string.notification_new_recipe_channel_id),
                getString(R.string.notification_new_recipe_channel_name),
                NotificationManager.IMPORTANCE_LOW));
        //channel1.setVibrationPattern(new long[]{150, 20, 150});
        //channel1.enableVibration(true);

        channels.add(new NotificationChannel(getString(R.string.notification_comment_channel_id),
                getString(R.string.notification_comment_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT));

        channels.add(new NotificationChannel(getString(R.string.notification_like_channel_id),
                getString(R.string.notification_like_channel_name),
                NotificationManager.IMPORTANCE_LOW));

        channels.add(new NotificationChannel(getString(R.string.notification_app_updates_channel_id),
                getString(R.string.notification_app_updates_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT));

        notificationManager.createNotificationChannels(channels);
    }

    /*@TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannelGroup(NotificationManager notificationManager) {
        NotificationChannelGroup notificationChannelGroup=
                new  NotificationChannelGroup(DEFAULT_GROUP,
                        getApplicationContext().getString(R.string.notification_default_channel_group_name));
        notificationManager.createNotificationChannelGroup(notificationChannelGroup);
    }*/

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        CrashLogger.e(TAG, "onDeletedMessages");
    }

    /*private static final int NOTIFICATION_DELETION_REQUEST = 2;
    private final String NOTIFICATION_DELETION_ACTION = "com.myapps.family_recipes.background.services.MyMessagingService.DELETE";
    private final IntentFilter intentFilter = new IntentFilter(NOTIFICATION_DELETION_ACTION);
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (NOTIFICATION_DELETION_ACTION.equals(action)) {
                int id = intent.getIntExtra(Constants.ID, 0);
                Log.e(TAG, "Notification deleted, " + id);
            }
        }
    };*/

    /*
    Open the notification channel settings

    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
    intent.putExtra(Settings.EXTRA_CHANNEL_ID, myNotificationChannel.getId());
    startActivity(intent);
     */
}
