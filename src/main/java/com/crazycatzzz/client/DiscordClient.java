package com.crazycatzzz.client;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;

import org.json.JSONObject;

// Custom Discord client
public class DiscordClient {
    private DiscordWebSocket ws; // WIP WebSocket for receiving messages etc...

    //private URL loginUrl;
    private URL meUrl;
    //private String settingsUrl = "https://discordapp.com/api/v6/users/@me/settings";
    //private String guildsUrl = "https://discordapp.com/api/v6/users/@me/guilds";
    private URL gatewayUrl;
    //private String logoutUrl = "https://discordapp.com/api/v6/auth/logout";
    //private String trackUrl = "https://discordapp.com/api/v6/track";
    //private String membersUrl = "https://discordapp.com/api/v6/guilds/{}/members";

    private String token; // user token

    private String webSocketGatewayQueryParams = "/?encoding=json&v=6"; // other websocket stuff

    private JSONObject me; // user info

    // HTTP Client for requests
    HttpClient client;

    public DiscordClient(String token) {
        this.token = token;

        try {
            client = HttpClient.newHttpClient();

            //loginUrl = new URL("https", "discordapp.com", "/api/v6/auth/login");
            meUrl = new URL("https", "discordapp.com", "/api/v6/users/@me");
            gatewayUrl = new URL("https", "discordapp.com", "/api/v6/gateway");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /*
     * The method login(email, password) does not pass the captcha
     * 
    @Deprecated
    public void login(String email, String password) {
        HttpRequest loginReq;
        
        // JSON Body
        BodyPublisher bp = BodyPublishers.ofString(
            new String(
                "{" +
                    "\"email\":\"" + email + "\"," +
                    "\"password\":\"" + password + "\"" +
                "}"
            )
        );

        HttpResponse<String> res;

        try {
            loginReq = HttpRequest.newBuilder()
                .uri(loginUrl.toURI())
                .POST(bp)
                .headers("Content-Type", "application/json")
                .build();
            res = client.send(loginReq, BodyHandlers.ofString());
            System.out.println(res.body());
        } catch (Exception e) {
            System.out.println(loginUrl);
            System.out.println("error in login");
            System.out.println(e);
        }
    }
    */

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
            msgStrings = "{\"messages\": " + res.body().toString() + "}";
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
            JSONObject resBody = new JSONObject(res.body().toString());
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

    // Send typing start
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
}