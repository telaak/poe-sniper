package poetrader;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocket {
    WebSocketClient poeTradeWebSocket;
    String poeTradeUrl;
    String newid;

    public WebSocket(String poeTradeURL) {
        this.poeTradeUrl = poeTradeURL;
        poeTradeWebSocket = null;
        try {
            poeTradeWebSocket = new WebSocketClient( new URI( "ws://live.poe.trade/" + poeTradeURL )) {
                @Override
                public void onMessage(String s) {
                    System.out.println(s);
                    newid = s;
                }

                @Override
                public void onOpen( ServerHandshake handshake ) {
                    System.out.println( "WebSocket open" );
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

    public void getNewId() {

    }
}

