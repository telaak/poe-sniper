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

@SuppressWarnings("SpellCheckingInspection")
public class WebSocket {
    WebSocketClient poeTradeWebSocket;
    WebSocketListener webSocketListener;
    HttpURLConnection connection;
    ResponseJson response;
    Document doc;
    String poeTradeUrl;
    @SuppressWarnings("SpellCheckingInspection")
    String newid;

    /**
     * The constructor creates the WebSocket connection and sets its listener methods, and finally connects
     * @param poeTradeSearchString The poe.trade search string to connect to
     */

    public WebSocket(final String poeTradeSearchString) {
        this.poeTradeUrl = poeTradeSearchString;
        poeTradeWebSocket = null;
        try {
            //noinspection SpellCheckingInspection
            poeTradeWebSocket = new WebSocketClient( new URI( "ws://live.poe.trade/" + poeTradeSearchString )) {
                @Override
                public void onMessage(String s) {
                    /* Sends a POST request to fetch the item data */
                    sendHttpPostRequest("http://poe.trade/search/" + poeTradeUrl + "/live","id=" + newid);
                    parseData();
                }

                @Override
                public void onOpen( ServerHandshake handshake ) {
                    System.out.println( "WebSocket open" );
                    /* When opening for the first time, sends a POST request with a negative id to fetch
                     * a working newid for messages
                     */
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

    /**
     * Sends a POST request and gives the response to be parsed by gson into a separate ResponseJson class.
     * Also updates the newid value for the next request
     * @param url The url that the request is sent to
     * @param parameters The parameters sent with the request
     */

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

    /** Parses the data from the ResponseJson's data node (HTML) into a jsoup document
     * The document is then scanned for 'item' classes and iterated through attribute values to generate a message
     * The message is then copied to the clipboard and sent to the Path of Exile window
     * Finally, the document is sent to its listeners
     */

    public void parseData() {
        doc = Jsoup.parse(response.data);
        Elements item = doc.select(".item");
        for(Element element : item) {
           // System.out.println(doc);
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
            if (hwnd != null) {
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

@SuppressWarnings("ALL")
class ResponseJson {
    @SuppressWarnings({"SpellCheckingInspection", "unused"})
    String newid;
    String count;
    String data;
}

interface WebSocketListener {
    void itemData(Document document);
}


