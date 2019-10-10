package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(ClientMain.class.getResource("/main.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("CloudBox");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
