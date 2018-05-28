package poetrader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;


public class Main extends Application {

    ArrayList<WebSocket> webSockets = new ArrayList<>();
    ArrayList<ObservableList<ItemData>> itemLists = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("poe-trader.fxml"));
        primaryStage.setTitle("PoE Trader");
        Group root = new Group();
        Scene scene = new Scene(root, 295, 200, Color.WHITE);
        primaryStage.getIcons().add(new Image("http://i0.kym-cdn.com/photos/images/original/001/058/457/fb2.jpg"));
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setSide(Side.LEFT);
        BorderPane borderPane = new BorderPane();
        //TODO: Clean this mess up, seriously
        for (int i = 0; i < 7; i++) {
            Tab tab = new Tab();
            tab.setText("Tab " + i);
            GridPane pane = new GridPane();
            final TextField poeTradeUrl = new TextField();
            poeTradeUrl.setPromptText("Insert poe.trade search string");
            poeTradeUrl.maxWidth(Double.MAX_VALUE);
            poeTradeUrl.maxHeight(Double.MAX_VALUE);
            final Button btn = new Button("Start live searching");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setMaxHeight(Double.MAX_VALUE);
            TableView items = new TableView();
            items.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            items.setEditable(true);
            TableColumn itemName = new TableColumn("Item name");
            itemName.setCellValueFactory(new PropertyValueFactory<ItemData, String>("itemName"));
            TableColumn characterName = new TableColumn("Character name");
            characterName.setCellValueFactory(new PropertyValueFactory<ItemData, String>("characterName"));
            TableColumn buyout = new TableColumn("Buyout");
            buyout.setCellValueFactory(new PropertyValueFactory<ItemData, String>("buyout"));
            ObservableList<ItemData> data = FXCollections.observableArrayList();
            itemLists.add(data);
            items.setItems(itemLists.get(i));
            items.getColumns().addAll(itemName,characterName,buyout);
            setButtonStartLiveSearch(btn,poeTradeUrl,tab);
            pane.add(btn,0,0);
            pane.add(poeTradeUrl,1,0);
            pane.add(items,0,1,2,1);
            tab.setContent(pane);
            ColumnConstraints halfConstraint = new ColumnConstraints();
            halfConstraint.setPercentWidth(50);
            RowConstraints rowConstraint1 = new RowConstraints();
            rowConstraint1.setPercentHeight(1);
            RowConstraints rowConstraint2 = new RowConstraints();
            rowConstraint2.setPercentHeight(99);
            pane.getColumnConstraints().addAll(halfConstraint, halfConstraint);
            pane.getRowConstraints().addAll(rowConstraint1,rowConstraint2);
            tabPane.getTabs().add(tab);
            tab.setId(String.valueOf(i));
        }
        borderPane.prefHeightProperty().bind(scene.heightProperty());
        borderPane.prefWidthProperty().bind(scene.widthProperty());
        borderPane.setCenter(tabPane);
        root.getChildren().add(borderPane);
        primaryStage.setScene(scene);
        primaryStage.show();

       // Thread api = new Thread(new APIThread());
       // api.start();
       // WebSocket ws = new WebSocket("iwokugosunotes");
    }

    public void buttonWebSocket(TextField textField, final Button button, final Tab tab) {
        final WebSocket webSocket = new WebSocket(textField.getText());
        webSocket.webSocketListener = new WebSocketListener() {
            @Override
            public void itemData(final Document document) {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        Elements item = document.select(".item");
                        for(Element element : item) {
                            String inGameName = element.attr("data-ign");
                            String itemName = element.attr("data-name");
                            String buyout = element.attr("data-buyout");
                            itemLists.get(Integer.parseInt(tab.getId())).add(new ItemData(itemName,inGameName,buyout));
                        }
                    }
                });
            }
        };
        webSockets.add(webSocket);
        setButtonStopLiveSearch(button, textField, webSocket, tab);
    }

    private void setButtonStopLiveSearch(final Button button, final TextField textField, final WebSocket webSocket, final Tab tab) {
        button.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                webSocket.disconnect();
                webSockets.remove(webSocket);
                setButtonStartLiveSearch(button, textField, tab);
                button.setText("Start live searching");
            }
        });
    }

    private void setButtonStartLiveSearch(final Button button, final TextField textField, final Tab tab) {
        button.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                buttonWebSocket(textField,button,tab);
                button.setText("Stop live searching");
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }

}
