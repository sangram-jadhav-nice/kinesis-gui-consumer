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
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("mainController.fxml")));
    primaryStage.setTitle("Kinesis Consumer");
    Scene scene = new Scene(root);
    scene.getStylesheets().add("styles/style.css");
    primaryStage.setScene(scene);
    primaryStage.setMaximized(true);
    primaryStage.getIcons().addAll(
      /*new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("images/icon-16.png"))),
      new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("images/icon-32.png"))),
      new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("images/icon-40.png"))),
      new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("images/icon-48.png"))),
      new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("images/icon-64.png"))),
      new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("images/icon-128.png"))),
      new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("images/icon-256.png"))),*/
      new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("images/icon-new.png")))

    );
    primaryStage.setOnCloseRequest(event -> Platform.runLater(() -> System.exit(0)));
    primaryStage.show();
  }


  public static void main(String[] args) {
    launch(args);
  }
}
