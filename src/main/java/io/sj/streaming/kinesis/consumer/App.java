package io.sj.streaming.kinesis.consumer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("mainController.fxml")));
        primaryStage.setTitle("Kinesis Consumer");
        Scene scene = new Scene(root);
        scene.getStylesheets().add("styles/style.css");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("images/icon.png"))));
        primaryStage.setOnCloseRequest(event -> Platform.runLater(() -> System.exit(0)));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
