package com.myapps.ron.family_recipes.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.ui.activities.SplashActivity;
import com.myapps.ron.family_recipes.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by ronginat on 10/02/2019.
 */
public class MyMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyMessagingService.class.getSimpleName();

    @SuppressWarnings("FieldCanBeLocal")
    private final String DEFAULT_GROUP = "com.myapps.ron.family_recipes.DEFAULT";

    private List<Notification> notifications;

    @Override
    public void onCreate() {
        super.onCreate();
        notifications = new ArrayList<>();
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notifications.clear();
        notifications = null;
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e(TAG, "onMessageReceived");
        Log.e(TAG, remoteMessage.getData().toString());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification: " + remoteMessage.getNotification().toString());
            remoteMessage.getNotification().getClickAction();
        }

        if (remoteMessage.getNotification() == null && remoteMessage.getData() != null) {
            /*
            Instead of sending a “notification” entry in the payload, change it for a “data” entry.
            In this way the notifications will ALWAYS be managed by the app through onMessageReceived
            https://medium.com/@cdmunoz/working-easily-with-fcm-push-notifications-in-android-e1804c80f74
            */
            sendNotification(remoteMessage.getData());
        }

    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.e(TAG, "Refreshed token: " + token);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param data FCM message data received.
     */
    private void sendNotification(@NonNull Map<String, String> data) {
        Log.e(TAG, "sendNotification");
        String channelId = data.get(Constants.CHANNEL);
        if (channelId == null)
            channelId = getString(R.string.notification_new_recipe_channel_name);

        // create notification click intent
        PendingIntent pendingIntent = getIntentToStartActivity(data, channelId);
        PendingIntent deletionIntent = getDeletionIntent(channelId);

        // set the notification UI
        NotificationCompat.Builder notificationBuilder = getNotificationBuilderUI(data, channelId, pendingIntent, deletionIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels(notificationManager);
        }

        // send notification to the user
        notificationManager.notify(Integer.parseInt(channelId) /* ID of notification */, notificationBuilder.build());
    }

    private PendingIntent getIntentToStartActivity(Map<String, String> data, String channelId) {
        Intent intent;
        if (data.get(Constants.ID) != null) {
            intent = new Intent(this, SplashActivity.class);
            /*Bundle bundle = new Bundle();
            bundle.putString(Constants.RECIPE_ID, recipeId);
            bundle.putBoolean("notification", true);
            bundle.putString("channel", channelId);
            intent.putExtras(bundle);*/
            intent.putExtra(Constants.RECIPE_ID, data.get(Constants.ID));
            intent.putExtra(Constants.NOTIFICATION, true);
            intent.putExtra(Constants.CHANNEL, channelId); // whether to fetch new recipes or just load comments normally
        }
        else
            intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        return PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
    }

    private PendingIntent getDeletionIntent(String notificationID) {
        Intent intent = new Intent(NOTIFICATION_DELETION_ACTION);
        intent.putExtra(Constants.ID, notificationID);
        return PendingIntent.getBroadcast(this, NOTIFICATION_DELETION_REQUEST, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    private NotificationCompat.Builder getNotificationBuilderUI(Map<String, String> data, String channelId, PendingIntent pendingIntent, PendingIntent deletionIntent) {
        // set the notification UI
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_status_logo)
                        .setContentTitle(data.get(Constants.TITLE))
                        .setContentText(data.get(Constants.BODY))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setColorized(true)
                        .setColor(ContextCompat.getColor(this, R.color.logo_foreground1))
                        .setGroupSummary(true)
                        .setGroup(DEFAULT_GROUP)
                        .setContentIntent(pendingIntent)
                        .setDeleteIntent(deletionIntent);
        inboxStyle.setBigContentTitle("New events from recipes");
        inboxStyle.addLine(data.get(Constants.TITLE));
        notificationBuilder.setStyle(inboxStyle);
        if (channelId.equals(getString(R.string.notification_new_recipe_channel_id))) {
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_post_black));
        }
        return notificationBuilder;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannels(NotificationManager notificationManager) {
        List<NotificationChannel> channels = new ArrayList<>();
        //createNotificationChannelGroup(notificationManager);
        NotificationChannel channel1 = new NotificationChannel(getString(R.string.notification_new_recipe_channel_id),
                getString(R.string.notification_new_recipe_channel_name),
                NotificationManager.IMPORTANCE_LOW);
        //channel1.setGroup(DEFAULT_GROUP);
        channel1.setVibrationPattern(new long[]{150, 150, 100});
        channels.add(channel1);
        NotificationChannel channel2 = new NotificationChannel(getString(R.string.notification_comment_channel_id),
                getString(R.string.notification_comment_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        //channel2.setGroup(DEFAULT_GROUP);
        channels.add(channel2);

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
        Log.e(TAG, "onDeletedMessages");
    }

    private static final int NOTIFICATION_DELETION_REQUEST = 2;
    private final String NOTIFICATION_DELETION_ACTION = "com.myapps.ron.family_recipes.services.MyMessagingService.DELETE";
    private final IntentFilter intentFilter = new IntentFilter(NOTIFICATION_DELETION_ACTION);
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (NOTIFICATION_DELETION_ACTION.equals(action)) {
                String id = intent.getStringExtra(Constants.ID);
                Log.e(TAG, "Notification deleted, " + id);
            }
        }
    };

    /*
    Open the notification channel settings

    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
    intent.putExtra(Settings.EXTRA_CHANNEL_ID, myNotificationChannel.getId());
    startActivity(intent);
     */
}
