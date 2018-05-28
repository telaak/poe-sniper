package poetrader;

import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

import static java.lang.Thread.sleep;

public class WebSocket {
    WebSocketClient poeTradeWebSocket;
    WebSocketListener webSocketListener;
    HttpURLConnection connection;
    ResponseJson response;
    Document doc;
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
                    parseData();
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
        connect();
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
                response = new Gson().fromJson(in, ResponseJson.class);
                newid = response.newid;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
    }

    public void parseData() {
        doc = Jsoup.parse(response.data);
        Elements item = doc.select(".item");
        for(Element element : item) {
            String inGameName = element.attr("data-ign");
            String itemName = element.attr("data-name");
            String buyout = element.attr("data-buyout");
            String league = element.attr("data-league");
            String tabName = element.attr("data-tab");
            String xPos = element.attr("data-x");
            String yPos = element.attr("data-y");
            StringSelection selection = new StringSelection("@" + inGameName + " Hi I would like to buy your " + itemName + " listed for " + buyout + " in " + league + " stash tab " + tabName + " position: left " + xPos + ", top "+ yPos);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            HWND hwnd = User32.INSTANCE.FindWindow(null, "Path of Exile");
            if (hwnd == null) {
                System.out.println("Window is not running");
            }
            else{
                try {
                    Robot robot = new Robot();
                    User32.INSTANCE.SetFocus(hwnd);
                    User32.INSTANCE.ShowWindow(hwnd, 9 );
                    User32.INSTANCE.SetForegroundWindow(hwnd);
                    sleep(500);
                    robot.keyPress(KeyEvent.VK_ENTER);
                    robot.keyRelease(KeyEvent.VK_ENTER);
                    robot.keyPress(KeyEvent.VK_CONTROL);
                    robot.keyPress(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_CONTROL);
                    robot.keyRelease(KeyEvent.VK_V);
                    robot.keyPress(KeyEvent.VK_ENTER);
                    robot.keyRelease(KeyEvent.VK_ENTER);
                } catch (AWTException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        sendItemUpdateData();
    }

    public void sendItemUpdateData() {
        webSocketListener.itemData(doc);
    }
}

class ResponseJson {
    String newid;
    String count;
    String data;
}

interface WebSocketListener {
    void itemData(Document document);
}


