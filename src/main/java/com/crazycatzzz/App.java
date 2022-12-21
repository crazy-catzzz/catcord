package com.crazycatzzz;

import com.crazycatzzz.client.DiscordClient;

import org.json.JSONArray;
import org.json.JSONObject;

public final class App {
    public static void main(String[] args) {
        String token = "YOUR TOKEN GOES HERE";  // NOTE: This is a user token, NOT a bot token!
        DiscordClient client = new DiscordClient(token);

        client.getMe(); // GET USER INFO

        // SEND MSG/SHOW LAST MSGS
        String channel = "CHANNEL ID GOES HERE";
        String content = "Hello World!";
        //printChannelMsgs(channel, client);    // Uncomment if you want to print the last 10 msgs or so
        //client.sendMessage(channel, content, false, "123");
        //client.sendStartTyping(channel);
    }

    // Prints the last 10 msgs sent in a channel
    private static void printChannelMsgs(String id, DiscordClient client) {
        JSONObject msgs = client.getMessages(id, 10);

        JSONArray msgArray = msgs.getJSONArray("messages");

        // This for goes backwards since the messages in msgArray are in reverse order, from the last to the first
        for (int i = msgArray.length() - 1; i >= 0; i--) {
            String author = msgArray.getJSONObject(i).getJSONObject("author").getString("username");
            String content = msgArray.getJSONObject(i).getString("content");

            System.out.println(author + ": " + content);
        }
    }
}
