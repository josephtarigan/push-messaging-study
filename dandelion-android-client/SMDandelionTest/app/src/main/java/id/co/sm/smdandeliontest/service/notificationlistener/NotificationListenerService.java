package id.co.sm.smdandeliontest.service.notificationlistener;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

import id.co.sm.dandelion.client.WebSocketClient;
import id.co.sm.dandelion.frame.JsonWebSocketFrame;
import id.co.sm.dandelion.observer.WebSocketClientObserver;

/**
 * Joseph Tarigan
 */
public class NotificationListenerService extends Service implements WebSocketClientObserver {

    private static final String CHANNEL_ID = "SM_NOTIF_CHANNEL";
    public static final int NOTIF_ID = 117788;

    private String id;
    private String wsUrl;
    private WebSocketClient wsClient;

    private NotificationCompat.Builder mBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Takes ID and WS_URL from the intent
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Notification Listener is Starting", Toast.LENGTH_SHORT);
        try {
            this.id = intent.getStringExtra("ID");
        } catch (Exception e) {
            // what should we do if the system cannot find the ID from the intent?
            this.id = UUID.randomUUID().toString();
        }

        try {
            this.wsUrl = intent.getStringExtra("WS_URL");
        } catch (Exception e) {
            throw new RuntimeException("The service cannot find WS_URL in the intent");
        }

        // start the client
        this.startWsClient();

        // put the same intent again
        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            if (wsClient != null) {
                wsClient.shutdown();
                wsClient = null;
            }
        } catch (InterruptedException e) {
            Log.e("ERROR", e.getMessage(), e);
        }
    }

    private void startWsClient () {
        wsClient = new WebSocketClient(this.id, this.wsUrl, this);
        wsClient.start();
    }

    @Override
    public void onMessageReceived(JsonWebSocketFrame jsonWebSocketFrame) {
        // only catch message
        if (jsonWebSocketFrame.getMessageContent().equals(JsonWebSocketFrame.MessageType.MESSAGE.getType())) {
            mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(android.support.compat.R.drawable.notify_panel_notification_icon_bg)
                    .setContentTitle("SM Notification")
                    .setContentText(jsonWebSocketFrame.getMessageContent())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(jsonWebSocketFrame.getMessageContent()))
                    .setVibrate(new long[]{300, 300})
                    .setLights(Color.YELLOW, 500, 200)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIF_ID, mBuilder.build());
        }
    }

    @Override
    public void onClientConnected() {
        // do nothing
        Log.d("CONNECTION", "Client is connected");
    }

    @Override
    public void onClientDisconnected() {
        // restart the connection?
        Log.d("CONNECTION", "Client is disconnected");

        // start a new one
        this.startWsClient();
    }

    @Override
    public void onConnectingFail() {
        // do nothing
        Log.d("CONNECTION", "Connecting attempt is fail");
    }

    @Override
    public void onClientReconnecting() {
        // do nothing
        Log.d("CONNECTION", "Reconnecting");
    }

    @Override
    public void onConnectionSuccess() {
        // do nothing
        Log.d("CONNECTION", "Connecting is success");
    }

    @Override
    public void onExceptionCaught(Throwable throwable) {
        // do nothing
        Log.e("WS_CLIENT", throwable.getMessage());
    }
}