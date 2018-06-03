package id.co.sm.smdandeliontest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import id.co.sm.dandelion.client.WebSocketClient;
import id.co.sm.smdandeliontest.service.notificationlistener.NotificationListenerService;

public class MainActivity extends AppCompatActivity {

    private TelephonyManager telephonyManager;
    private String myId = "";
    private String wsUrl = "ws://192.168.43.105:8800";
    //private String wsUrl = "ws://smpst-pcitg150.corp.sm.co.id:8800";
    private WebSocketClient wsClient;
    private WebSocketListener wsListener;
    private boolean isConnected = false;
    private boolean isTryingToConnect = false;

    private ImageView imagePlaceholder;
    private EditText serverAddress;
    private TextView connectionStatus;
    private EditText input;
    private EditText serverResponse;

    private Button connectButton;
    private Button sendMessageButton;

    private Intent notificationListenerService;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init
        init ();

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        serverAddress.setText(wsUrl);
        wsListener = new WebSocketListener(this);

        // set my id
        myId = ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId() == null ? UUID.randomUUID().toString() : ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serverAddress.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Server address is empty", Toast.LENGTH_LONG);
                    return;
                }

                if (isConnected) {
                    try {
                        wsClient.shutdown();
                        isConnected = false;
                        isTryingToConnect = false;
                        connectionStatus.setText(R.string.disconnected);
                        connectButton.setText(R.string.connect);
                    } catch (Exception e) {
                        Log.e("ERROR", e.getMessage(), e);
                    }
                } else {
                    connectionStatus.setText(R.string.connecting);
                    wsClient = new WebSocketClient(myId, serverAddress.getText().toString(), wsListener);
                    final Thread startClientThread = new Thread(new ConnectButtonHandler(wsClient));
                    startClientThread.start();
                }
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wsClient.isConnected()) {
                    wsClient.sendMessage(input.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "Client is not connected", Toast.LENGTH_LONG);
                }
            }
        });

        // service
        startNotificationListenerService();
    }

    private void init () {
        serverAddress = findViewById(R.id.server_address);
        connectionStatus = findViewById(R.id.server_status);
        serverResponse = findViewById(R.id.server_response);
        imagePlaceholder = findViewById(R.id.image_placeholder);
        input = findViewById(R.id.message_input);

        connectButton = findViewById(R.id.connect_button);
        sendMessageButton = findViewById(R.id.send_button);
    }

    private void startNotificationListenerService () {
        notificationListenerService = new Intent(this, NotificationListenerService.class);
        notificationListenerService.putExtra("ID", this.myId);
        notificationListenerService.putExtra("WS_URL", this.wsUrl);

        startService(notificationListenerService);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationListenerService != null) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NotificationListenerService.NOTIF_ID);
            stopService(this.notificationListenerService);
        }
    }

    public void onClientDisconnected() {
        isConnected = false;
        isTryingToConnect = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionStatus.setText(R.string.disconnected);
                connectButton.setText(R.string.connect);
            }
        });
    }

    public void onClientReconnecting () {
        isConnected = false;
        isTryingToConnect = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionStatus.setText(R.string.reconnecting);
                connectButton.setText(R.string.connect);
            }
        });
    }

    public void onClientConnectFail () {
        isConnected = false;
        isTryingToConnect = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionStatus.setText(R.string.connection_fail);
                connectButton.setText(R.string.connect);
            }
        });
    }

    public void onClientConnected () {
        isConnected = true;
        isTryingToConnect = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionStatus.setText(R.string.connected);
                connectButton.setText(R.string.disconnect);
            }
        });
    }

    public ImageView getImagePlaceholder() {
        return imagePlaceholder;
    }

    public EditText getServerResponse() {
        return serverResponse;
    }

    public TextView getConnectionStatus() {
        return connectionStatus;
    }

    public Button getConnectButton() {
        return connectButton;
    }

    private void setIsTryingToConnect (boolean state) {
        this.isTryingToConnect = state;
    }

    class ConnectButtonHandler extends Activity implements Runnable {

        private WebSocketClient wsClient;

        public ConnectButtonHandler (final WebSocketClient wsClient) {
            this.wsClient = wsClient;
        }

        @Override
        public void run() {
            try {
                if (!isTryingToConnect) {
                    isTryingToConnect = true;
                    wsClient.start();
                } else {
                    isTryingToConnect = false;
                    wsClient.shutdown();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectionStatus.setText(R.string.disconnected);
                    }
                });
                return;
            }
        }
    }
}