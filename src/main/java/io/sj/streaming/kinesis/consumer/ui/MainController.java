package io.sj.streaming.kinesis.consumer.ui;

import com.google.gson.JsonElement;
import io.sj.streaming.kinesis.consumer.kinesis.KinesisConsumer;
import io.sj.streaming.kinesis.consumer.parsing.JsonParser;
import io.sj.streaming.kinesis.consumer.parsing.RecordProcessorDelegate;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @FXML
  public TextField txtFieldStream;
  @FXML
  public TextArea txtAreaSchema;
  @FXML
  public ComboBox<String> cmbType;
  @FXML
  public Button btnStart;
  @FXML
  private TreeTableView<Pair<String, JsonElement>> ttViewContent;

  private KinesisConsumer kinesisConsumer;
  private boolean isConsumerStarted;
  private JsonParser parser = new JsonParser();


  public MainController() {
    // Default Constructor
  }

  @FXML
  public void initialize() {
    ttViewContent.setRowFactory(param -> new TreeTableRowContainer());
    ttViewContent.getColumns().clear();
    ttViewContent.getColumns().addAll(parser.getColumns());
    TreeItem dummyRoot = new TreeItem();
    ttViewContent.setRoot(dummyRoot);
    ttViewContent.setShowRoot(false);
    Platform.runLater(() -> ttViewContent.requestFocus());
  }

  @FXML
  public void startConsumers(MouseEvent mouseEvent) {
    if (!isConsumerStarted) {
      onConsumerStart();
    } else {
      onConsumerStop();
    }
  }

  @FXML
  public void clear(MouseEvent mouseEvent) {
    ttViewContent.getRoot().getChildren().clear();
  }


  private void onConsumerStart() {
    String stream = txtFieldStream.getText();
    String schema = txtAreaSchema.getText();
    String type = cmbType.getValue();
    if ("AVRO".equals(type) && StringUtils.isEmpty(schema)) {
      showAlert("Schema is required");
    } else {
      kinesisConsumer = new KinesisConsumer(ttViewContent.getRoot().getChildren());
      kinesisConsumer.withType(RecordProcessorDelegate.valueOf(type)).withStream(stream).withSchema(schema).runConsumer();
      kinesisConsumer.setOnCancelled(this::guiOnStop);
      guiOnStart();
    }
  }

  private void guiOnStop() {
    btnStart.setDisable(false);
    btnStart.setText("Start Consumer");
    txtFieldStream.setDisable(false);
    kinesisConsumer.stopConsumer();
  }

  private void guiOnStart() {
    isConsumerStarted = true;
    btnStart.setText("Stop Consumer");
    txtFieldStream.setDisable(true);
    ObservableList<TreeItem<Pair<String, JsonElement>>> items = ttViewContent.getRoot().getChildren();
    if (items != null) {
      items.clear();
    }
  }

  private void onConsumerStop() {
    btnStart.setDisable(true);
    kinesisConsumer.stopConsumer();
    isConsumerStarted = false;
    btnStart.setText("Start Consumer");
    btnStart.setDisable(false);
    txtFieldStream.setDisable(false);
  }

  private void showAlert(String text) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Information");
      alert.setHeaderText(null);
      alert.setContentText(text);
      alert.showAndWait();
    });
  }
}
