package id.co.sm.smdandeliontest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import id.co.sm.dandelion.frame.JsonWebSocketFrame;
import id.co.sm.dandelion.observer.WebSocketClientObserver;

public class WebSocketListener extends Activity implements WebSocketClientObserver {

    private MainActivity mainActivity;

    public WebSocketListener (MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onMessageReceived(final JsonWebSocketFrame jsonWebSocketFrame) {
        if (jsonWebSocketFrame.getMessageType().equals(JsonWebSocketFrame.MessageType.MESSAGE.toString())) {
            if (mainActivity.getServerResponse() != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.getServerResponse().setText(jsonWebSocketFrame.getMessageContent());
                    }
                });
            }
        } else if (jsonWebSocketFrame.getMessageType().equals(JsonWebSocketFrame.MessageType.BASE64.toString())) {
            // jpg
            String base64String = jsonWebSocketFrame.getMessageContent();
            byte[] bytes = Base64.decode(base64String, Base64.DEFAULT);

            // decode to image
            final Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.getImagePlaceholder().setImageBitmap(bmp);
                }
            });
        }
    }

    @Override
    public void onClientConnected() {
        mainActivity.onClientConnected();
    }

    @Override
    public void onClientDisconnected() {
        Log.w("WARN", "Client disconnected");
        mainActivity.onClientDisconnected();
    }

    @Override
    public void onConnectingFail() {
        mainActivity.onClientConnectFail();
    }

    @Override
    public void onClientReconnecting() {
        mainActivity.onClientReconnecting();
    }

    @Override
    public void onConnectionSuccess() {
        mainActivity.onClientConnected();
    }

    @Override
    public void onExceptionCaught(final Throwable throwable) {
        Log.e("ERROR", throwable.getMessage());
        if (mainActivity.getServerResponse() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.getServerResponse().setText(throwable.getMessage());
                }
            });
        }
    }
}