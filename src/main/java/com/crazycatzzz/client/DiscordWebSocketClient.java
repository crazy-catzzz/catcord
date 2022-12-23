package com.crazycatzzz.client;

import java.net.URI;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletionStage;

import org.json.JSONObject;

/*
 * WIP
*/

public class DiscordWebSocketClient {
    private WebSocket ws;

    private int heartbeatInterval;
    private int lastSequenceNumber;

    private JSONObject current;

    public DiscordWebSocketClient(DiscordClient client) {
        Listener listener = new Listener() {
            @Override
            public void onOpen(WebSocket socket) {
                System.out.println("Discord WebSocket connected!");
                socket.request(1);
            }

            @Override
            public CompletionStage<?> onText(WebSocket socket, CharSequence data, boolean last) {
                //System.out.print(data);
                current = new JSONObject(data.toString());
                lastSequenceNumber = current.getInt("s");


                switch (current.getInt("op")) {
                    case 10:
                        System.out.println("Hello received!");
                        heartbeatInterval = current.getJSONObject("d").getInt("heartbeat_interval");
                        break;
                    case 1:
                        System.out.println("Heartbeat received");
                        JSONObject toSend = new JSONObject()
                                            .put("op", 1)
                                            .put("d", lastSequenceNumber);
                        socket.sendText(toSend.toString(), true);
                        break;
                }

                socket.request(1);
                return null;
            }
        };

        try {
            ws = client.client.newWebSocketBuilder().buildAsync(new URI(client.getWebSocketGateway()), listener).join();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public JSONObject getCurrent() {
        return current;
    }
}
