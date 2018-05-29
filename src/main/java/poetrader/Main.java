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

    ArrayList<ObservableList<ItemData>> itemLists = new ArrayList<>();
    ArrayList<WebSocket> webSockets = new ArrayList<>();
    Group root = new Group();
    Scene scene = new Scene(root, 800, 600, Color.WHITE);
    ColumnConstraints halfConstraint;
    RowConstraints rowConstraint;
    BorderPane borderPane;
    TabPane tabPane;

    @Override
    public void start(Stage primaryStage) throws Exception{
        setUpConstraints();
        setUpTabPane();
        setUpBorderPane();
        addTabs(5);
        setUpStage(primaryStage,scene);
    }

    /**
     * Adds tabs with a TextField for the poe.trade search string to use, a button to toggle the WebSocket on and off
     * Also adds a TableView with 3 columns for item names, character names and buyout values
     * The elements are added into a GridPane with constraints; first row contains the TextField and button at 50% width
     * The second row has the TableView with factories for the ItemData class (parsed data from POST responses)
     * @param amount Amount of tabs to add
     */

    public void addTabs(int amount) {
        for(int i = 0; i < amount; i++) {
            Tab tab = new Tab("Tab " + i);
            tab.setId(String.valueOf(i));

            final TextField poeTradeUrl = new TextField();
            poeTradeUrl.setPromptText("Insert poe.trade search string");

            final Button toggleSearchButton = new Button("Start live searching");
            toggleSearchButton.setMaxWidth(Double.MAX_VALUE);
            setButtonStartLiveSearch(toggleSearchButton,poeTradeUrl,tab);

            TableView items = new TableView();
            items.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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

            GridPane pane = new GridPane();
            pane.add(toggleSearchButton,0,0);
            pane.add(poeTradeUrl,1,0);
            pane.add(items,0,1,2,1);

            pane.getColumnConstraints().addAll(halfConstraint,halfConstraint);
            pane.getRowConstraints().addAll(new RowConstraints(),rowConstraint);
            tab.setContent(pane);
            tabPane.getTabs().add(tab);
        }
    }

    public void setUpBorderPane() {
        borderPane = new BorderPane();
        borderPane.prefHeightProperty().bind(scene.heightProperty());
        borderPane.prefWidthProperty().bind(scene.widthProperty());
        borderPane.setCenter(tabPane);
        root.getChildren().add(borderPane);
    }

    public void setUpTabPane() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);        /* Removes the option to close tabs */
        tabPane.setSide(Side.LEFT);                                               /* Aligns the tabs to the left */
    }

    public void setUpStage(Stage stage, Scene scene) {
        stage.setTitle("PoE Trader");
        stage.getIcons().add(new Image("http://i0.kym-cdn.com/photos/images/original/001/058/457/fb2.jpg")); // kot
        stage.setScene(scene);
        stage.show();
    }

    public void setUpConstraints() {
        halfConstraint = new ColumnConstraints();
        halfConstraint.setPercentWidth(50);                                      /* 50% width for columns, 2 per row */
        rowConstraint = new RowConstraints();
        rowConstraint.setVgrow(Priority.ALWAYS);                                 /* for TableView, grows to fill row */
    }

    /**
     * Adds a poe.trade WebSocket connection when called by a button press
     * Also attaches a listener to the WebSocket to update the TableView when new items are found
     * @param textField The TextField containing the poe.trade search url
     * @param button The button calling this method
     * @param tab The tab the button is in, used for locating the TableView inside the tab
     */

    public void buttonWebSocket(TextField textField, final Button button, final Tab tab) {
        /* Creates the WebSocket from the TextField i.e. the poe.trade search string */
        final WebSocket webSocket = new WebSocket(textField.getText());
        webSocket.webSocketListener = new WebSocketListener() {
            @Override
            public void itemData(final Document document) {
                /* As the code is running in the WebSocket thread, it has to be sent to be ran later in the UI thread */
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
        setButtonStopLiveSearch(button, textField, webSocket, tab);              /* Switches the button's listener */
    }

    /**
     * Stops the WebSocket connection and switches the button bag to start connections
     * @param button The button calling this method, also the one to be changed
     * @param textField The TextField containing the poe.trade search string
     * @param webSocket The WebSocket to be closed
     * @param tab The tab where this button calling this is located
     */

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

    /**
     * Sets the button's action to call the WebSocket adding method
     * @param button The button calling this method, also the one to be changed
     * @param textField The TextField containing the poe.trade search string
     * @param tab The tab where this button calling this is located
     */

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
