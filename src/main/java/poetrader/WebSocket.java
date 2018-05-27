package poetrader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class WebSocket {
    WebSocketClient poeTradeWebSocket;
    HttpURLConnection connection;
    String poeTradeUrl;
    String newid;

    public WebSocket(final String poeTradeURL) {
        this.poeTradeUrl = poeTradeURL;
        poeTradeWebSocket = null;
        try {
            poeTradeWebSocket = new WebSocketClient( new URI( "ws://live.poe.trade/" + poeTradeURL )) {
                @Override
                public void onMessage(String s) {
                    sendHttpPostRequest("http://poe.trade/search/" + poeTradeUrl + "/live","id=" + newid);
                }

                @Override
                public void onOpen( ServerHandshake handshake ) {
                    System.out.println( "WebSocket open" );
                    sendHttpPostRequest("http://poe.trade/search/" + poeTradeUrl + "/live","id=-1");
                }

                @Override
                public void onClose( int code, String reason, boolean remote ) {
                    System.out.println( "WebSocket closed" );
                }

                @Override
                public void onError( Exception ex ) {
                    ex.printStackTrace();
                }

            };
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        poeTradeWebSocket.connect();
    }

    public void disconnect() {
        poeTradeWebSocket.close();
    }

    public void sendHttpPostRequest(String url, String parameters) {
        byte[] postData = parameters.getBytes(StandardCharsets.UTF_8);
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Java client");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                ResponseJson response = new Gson().fromJson(in, ResponseJson.class);
                System.out.print(response.data);
                newid = response.newid;
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
    }
}

class ResponseJson {
    String newid;
    String count;
    String data;
}


