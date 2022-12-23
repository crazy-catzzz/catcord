package com.crazycatzzz.client;

import java.net.URL;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletionStage;

import org.json.JSONArray;
import org.json.JSONObject;

// Custom Discord client
public class DiscordClient {
    private WebSocket ws; // WIP WebSocket for receiving messages etc...

    private URL meUrl;
    //private String settingsUrl = "https://discordapp.com/api/v6/users/@me/settings";
    //private String guildsUrl = "https://discordapp.com/api/v6/users/@me/guilds";
    private URL guildsUrl;
    private URL gatewayUrl;
    //private String logoutUrl = "https://discordapp.com/api/v6/auth/logout";
    //private String trackUrl = "https://discordapp.com/api/v6/track";

    private String token; // user token

    private String webSocketGatewayQueryParams = "/?encoding=json&v=6"; // other websocket stuff

    private JSONObject me; // user info

    private JSONObject current;
    private int heartbeatInterval;
    private int lastSequenceNumber;

    // HTTP Client for requests
    HttpClient client;

    public DiscordClient(String token) {
        this.token = token;

        try {
            client = HttpClient.newHttpClient();

            meUrl = new URL("https", "discordapp.com", "/api/v6/users/@me");
            guildsUrl = new URL("https", "discordapp.com", "/api/v6/users/@me/guilds");
            gatewayUrl = new URL("https", "discordapp.com", "/api/v6/gateway");

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

                    switch (current.getInt("op")) {
                        case 11:
                            System.out.println("Gateway acknowledged heartbeat!");
                            break;
                        case 10:
                            System.out.println("Hello received!");
                            heartbeatInterval = current.getJSONObject("d").getInt("heartbeat_interval");

                            JSONObject firstHeartbeat = new JSONObject()
                                                        .put("op", 1)
                                                        .put("d", lastSequenceNumber);

                            /*try {
                                Thread.sleep(Math.round(heartbeatInterval * Math.random()));
                            } catch (Exception e) {
                                System.out.println(e);
                            }*/
                            System.out.println("Sending first heartbeat");
                            socket.sendText(firstHeartbeat.toString(), true);

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
            ws = client.newWebSocketBuilder().buildAsync(new URI(getWebSocketGateway() + webSocketGatewayQueryParams), listener).join();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void heartbeatLoop() {
        while (!ws.isInputClosed() && !ws.isOutputClosed()) {
            try {
                Thread.sleep(heartbeatInterval);
            } catch (Exception e) {
                System.out.println(e);
            }
            System.out.println("Sending heartbeat...");
            JSONObject heartbeat = new JSONObject()
                                   .put("op", 1)
                                   .put("d", lastSequenceNumber);

            ws.sendText(heartbeat.toString(), true);
        }
    }

    // Uses the user token to get user info
    public void getMe() {
        System.out.println(meUrl);

        HttpRequest req;
        HttpResponse<String> res;

        try {
            req = HttpRequest.newBuilder()
                .uri(meUrl.toURI())
                .GET()
                .headers("Authorization", token)
                .build();
            res = client.send(req, BodyHandlers.ofString());
            me = new JSONObject(res.body().toString());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Uses a channel ID to get the last messages in a channel
    // NOTE: The array is backwards! It goes from the last message to the first message
    public JSONObject getMessages(String channelID, int limit) {
        URL reqURL;

        HttpRequest req;
        HttpResponse<String> res;

        String msgStrings = "";

        try {
            reqURL = new URL("https", "discordapp.com", "/api/v6/channels/" + channelID + "/messages?limit=" + limit);
            req = HttpRequest.newBuilder()
                .uri(reqURL.toURI())
                .GET()
                .headers("Authorization", token)
                .build();
            res = client.send(req, BodyHandlers.ofString());
            msgStrings = "{\"messages\": " + res.body() + "}";
        } catch (Exception e) {
            System.out.println(e);
        }

        JSONObject msgs = new JSONObject(msgStrings);
        return msgs;
    }

    // WIP will be used when I implement DiscordWebSocket
    private String getWebSocketGateway() {
        HttpRequest req;
        HttpResponse<String> res;

        try {
            req = HttpRequest.newBuilder()
                .uri(gatewayUrl.toURI())
                .GET()
                .headers("Authorization", token)
                .build();
            res = client.send(req, BodyHandlers.ofString());
            JSONObject resBody = new JSONObject(res.body());
            return resBody.getString("url");
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    // Sends a message to a channel, needs a channel ID
    public void sendMessage(String channelId, String messageContent, boolean tts, String nonce) {
        String body = new JSONObject()
                    .put("content", messageContent)
                    .put("tts", tts)
                    .put("nonce", nonce)
        .toString();

        BodyPublisher bp = BodyPublishers.ofString(body);

        HttpRequest req;
        HttpResponse<String> res;

        try {
            URL reqUrl = new URL("https", "discordapp.com", "/api/v6/channels/" + channelId + "/messages");
            req = HttpRequest.newBuilder()
                .uri(reqUrl.toURI())
                .POST(bp)
                .headers("Authorization", token, "Content-Type", "application/json")
                .build();
            res = client.send(req, BodyHandlers.ofString());
            System.out.println(res.body());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Send typing start to a channel
    public void sendStartTyping(String channelId) {
        String body = new JSONObject()
                    .toString();


        BodyPublisher bp = BodyPublishers.ofString(body);

        HttpRequest req;
        HttpResponse res;

        try {
            URL reqUrl = new URL("https", "discordapp.com", "/api/v6/channels/" + channelId + "/typing");
            req = HttpRequest.newBuilder()
                .uri(reqUrl.toURI())
                .POST(bp)
                .headers("Authorization", token)
                .build();
            res = client.send(req, BodyHandlers.ofString());
            System.out.println(res.statusCode());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Change presence
    // accepted presence values are: "idle", "online", "dnd" or "invisible"
    // WIP will be implemented along with DiscordWebSocket
    //public void changePresence(String presence) {}

    // Gets all servers the user is a member of
    public JSONObject getServers() {
        HttpRequest req;
        HttpResponse<String> res;

        JSONObject servers = null;

        try {
            req = HttpRequest.newBuilder()
                .uri(guildsUrl.toURI())
                .GET()
                .headers("Authorization", token)
                .build();
            res = client.send(req, BodyHandlers.ofString());
            String resBody = "{\"servers\":" + res.body() + "}";
            servers = new JSONObject(resBody);
        } catch (Exception e) {
            System.out.println(e);
        }

        return servers;
    }

    // Gets all the channels in a server
    public JSONObject getServerChannels(String serverId) {
        HttpRequest req;
        HttpResponse<String> res;

        JSONObject servers = null;

        try {
            URL channelsUrl = new URL("https", "discordapp.com", "/api/v6/guilds/" + serverId + "/channels");
            req = HttpRequest.newBuilder()
                .uri(channelsUrl.toURI())
                .GET()
                .headers("Authorization", token)
                .build();
            res = client.send(req, BodyHandlers.ofString());
            String resBody = "{\"servers\":" + res.body() + "}";
            servers = new JSONObject(resBody);
        } catch (Exception e) {
            System.out.println(e);
        }

        return servers;
    }

    // Gets all members in a server
    // Discord doesn't allow non-bot users access to the members endpoint, also the max amount of requests that can be made is 1000
    // WIP, will be implemented once DiscordWebSocket is done.
    //public void getServerMembers(String serverId, int limit) {}
}
