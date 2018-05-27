package poetrader;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;


public class Main extends Application {

    ArrayList<WebSocket> webSockets = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("poe-trader.fxml"));
        primaryStage.setTitle("PoE Trader");
        Group root = new Group();
        Scene scene = new Scene(root, 310, 200, Color.WHITE);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        BorderPane borderPane = new BorderPane();
        for (int i = 1; i <= 7; i++) {
            Tab tab = new Tab();
            tab.setText("Tab " + i);
            HBox hbox = new HBox();
            final TextField poeTradeUrl = new TextField();
            hbox.getChildren().add(poeTradeUrl);
            final Button btn = new Button("Start live searching");
            setButtonStartLiveSearch(btn,poeTradeUrl);
            hbox.getChildren().add(btn);
            hbox.setAlignment(Pos.CENTER);
            tab.setContent(hbox);
            tabPane.getTabs().add(tab);
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

    public void buttonWebSocket(TextField textField, final Button button) {
        final WebSocket webSocket = new WebSocket(textField.getText());
        webSockets.add(webSocket);
        setButtonStopLiveSearch(button, textField, webSocket);
    }

    private void setButtonStopLiveSearch(final Button button, final TextField textField, final WebSocket webSocket) {
        button.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                webSocket.disconnect();
                webSockets.remove(webSocket);
                setButtonStartLiveSearch(button, textField);
                button.setText("Start live searching");
            }
        });
    }

    private void setButtonStartLiveSearch(final Button button, final TextField textField) {
        button.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                buttonWebSocket(textField,button);
                button.setText("Stop live searching");
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }

}
