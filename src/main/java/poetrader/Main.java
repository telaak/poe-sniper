package poetrader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    API api = new API();

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("poe-trader.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        Thread api = new Thread(new APIThread());
       // api.start();

        WebSocket ws = new WebSocket("tesimetadayoki");
        ws.connect();
    }



    public static void main(String[] args) {
        launch(args);
    }

}
