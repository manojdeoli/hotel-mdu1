package com.hotel.blegateway;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import android.util.Log;

public class BLEGatewayService extends Service {
    private static final String TAG = "BLEGatewayService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "ble_gateway_channel";
    
    private PowerManager.WakeLock wakeLock;
    // TODO: Add your BLE Scanner and WebSocket Server instances here
    // private BLEScanner bleScanner;
    // private WebSocketServer wsServer;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        
        // Acquire wake lock to prevent CPU sleep
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "BLEGateway::WakeLock"
        );
        wakeLock.acquire();
        
        // TODO: Initialize your BLE Scanner and WebSocket Server here
        // bleScanner = new BLEScanner(this);
        // wsServer = new WebSocketServer(3001);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        
        createNotificationChannel();
        
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );
        
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hotel BLE Gateway")
            .setContentText("Scanning for beacons and running WebSocket server")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
        
        startForeground(NOTIFICATION_ID, notification);
        
        // TODO: Start your BLE scanning and WebSocket server here
        // bleScanner.startScanning();
        // wsServer.start();
        
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        
        // TODO: Stop BLE scanning and WebSocket server
        // bleScanner.stopScanning();
        // wsServer.stop();
        
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "BLE Gateway Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Keeps BLE scanning and WebSocket server running");
            channel.setShowBadge(false);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
