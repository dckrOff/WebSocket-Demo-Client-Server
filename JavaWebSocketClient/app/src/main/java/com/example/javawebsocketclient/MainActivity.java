package com.example.javawebsocketclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import tech.gusavila92.websocketclient.WebSocketClient;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;


public class MainActivity extends AppCompatActivity {
    private WebSocketClient webSocketClient;
    PubNub pubnub;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animal_sounds);

        createWebSocketClient();
        initPubNub();
    }

    private void createWebSocketClient() {
        URI uri;
        try {
            // Connect to local host
//            uri = new URI("ws://10.0.2.2:8080/websocket");
            uri = new URI("ws://192.168.1.14:8080/websocket");
//      uri = new URI("ws://84.54.70.238:8080/websocket");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
                webSocketClient.send("Hello World!");
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TextView textView = findViewById(R.id.animalSound);
                            textView.setText(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(Exception e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Connection Closed ");
                System.out.println("onCloseReceived");
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    // This method is called when a button is pressed
    public void sendMessage(View view) {
        // Get button ID
        switch (view.getId()) {
            case (R.id.dogButton):
                publishMessage("Woooof");
                break;

            case (R.id.catButton):
                publishMessage("Meooow");
                break;

            case (R.id.pigButton):
                publishMessage("Bork Bork");
                break;

            case (R.id.foxButton):
                publishMessage("Fraka-kaka-kaka");
                break;
        }
    }

    public void publishMessage(String animal_sound) {
        // Publish message to the global chanel
        pubnub.publish()
                .message(animal_sound)
                .channel("global_channel")
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // status.isError() to see if error happened and print status code if error
                        if (status.isError()) {
                            System.out.println("pub status code: " + status.getStatusCode());
                        }
                    }

                });
    }

    public void initPubNub() {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setPublishKey("pub-c-4133a38b-3ca5-4ed4-995f-a3ae69f58de8"); // REPLACE with your pub key
        pnConfiguration.setSubscribeKey("sub-c-37209b86-2979-49b7-87b7-1e07a0cdaf7e"); // REPLACE with your sub key
        pnConfiguration.setSecure(true);
        pubnub = new PubNub(pnConfiguration);

        // Listen to messages that arrive on the channel
        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pub, PNStatus status) {
            }

            @Override
            public void message(PubNub pub, final PNMessageResult message) {
                // Replace double quotes with a blank space
                final String msg = message.getMessage().toString().replace("\"", "");
                textView = findViewById(R.id.animalSound);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Display the message on the app
                            textView.setText(msg);
                        } catch (Exception e) {
                            System.out.println("Error");
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void presence(PubNub pub, PNPresenceEventResult presence) {
            }
        });

        // Subscribe to the global channel
        pubnub.subscribe()
                .channels(Arrays.asList("global_channel"))
                .execute();
    }
}
