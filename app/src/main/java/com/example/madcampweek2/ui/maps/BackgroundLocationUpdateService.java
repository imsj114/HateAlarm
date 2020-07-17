package com.example.madcampweek2.ui.maps;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.madcampweek2.MainActivity;
import com.example.madcampweek2.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

public class BackgroundLocationUpdateService extends Service {

    /**
     * Author：Hardik Talaviya
     * Date：  2019.08.3 2:30 PM
     * Describe:
     */

    private static final String TAG = "BackgroundLocation";
    private Context context;
    private FusedLocationProviderClient client;
    private LocationCallback locationCallback;
    private boolean stopService = false;
    private Handler handler;
    private Runnable runnable;
    private NotificationCompat.Builder builder = null;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        Log.e(TAG, "Background Service onCreate :: ");
        super.onCreate();
        context = this;

        handler = new Handler();
        runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    requestLocationUpdates();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    handler.postDelayed(this, TimeUnit.SECONDS.toMillis(2));
                }
            }
        };
        if (!stopService) {
            handler.postDelayed(runnable, TimeUnit.SECONDS.toMillis(2));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(TAG, "onTaskRemoved :: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand :: ");
        StartForeground();
        if (client != null) {
            client.removeLocationUpdates(locationCallback);
            Log.e(TAG, "Location Update Callback Removed");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "BackgroundService onDestroy :: ");
        stopService = true;
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        if (client != null) {
            client.removeLocationUpdates(locationCallback);
            Log.e(TAG, "Location Update Callback Removed");
        }
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setFastestInterval(100)
                .setInterval(200)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client = LocationServices.getFusedLocationProviderClient(this);


        final int[] permission = {ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)};
        if (permission[0] == PackageManager.PERMISSION_GRANTED) {

            final Location[] location = {new Location(LocationManager.GPS_PROVIDER)};
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    location[0] = locationResult.getLastLocation();

                    if (location[0] != null) {
                        Log.d(TAG, "location update " + location[0]);
                        Log.d(TAG, "location Latitude " + location[0].getLatitude());
                        Log.d(TAG, "location Longitude " + location[0].getLongitude());
                        Log.d(TAG, "Speed :: " + location[0].getSpeed() * 3.6);

                        if (notificationManager != null && client != null && !stopService) {
                            builder.setContentText("Your current location is " +  location[0].getLatitude() + "," + location[0].getLongitude());
                            notificationManager.notify(101, builder.build());
                        }
                    }
                }
            };
            client.requestLocationUpdates(request, locationCallback, null);
        }
    }

    /*-------- For notification ----------*/
    private void StartForeground() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String CHANNEL_ID = "channel_location";
        String CHANNEL_NAME = "channel_location";

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
            builder.setColorized(false);
            builder.setChannelId(CHANNEL_ID);
            builder.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            builder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE);
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        }
        builder.setOnlyAlertOnce(true);
        builder.setContentTitle(context.getResources().getString(R.string.app_name));
        builder.setContentText("Your current location is ");
        Uri notificationSound = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(notificationSound);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentIntent(pendingIntent);
        startForeground(101, builder.build());
    }
}