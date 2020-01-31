package io.sj.streaming.kinesis.consumer.parsing;


import com.google.gson.JsonElement;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JsonParser implements Parser<JsonElement> {
  private com.google.gson.JsonParser parser = new com.google.gson.JsonParser();

  public JsonParser() {
    // Default one
  }

  @Override
  public List<TreeTableColumn<Pair<String, JsonElement>, String>> getColumns() {
    TreeTableColumn<Pair<String, JsonElement>, String> columnTree = new TreeTableColumn<>("TREE");
    columnTree.setPrefWidth(300);
    columnTree.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getKey()));
    columnTree.setCellFactory(param -> new TextFieldTreeTableCell<>());

    TreeTableColumn<Pair<String, JsonElement>, String> columnType = new TreeTableColumn<>("TYPE");
    columnType.setPrefWidth(300);
    columnType.setCellValueFactory(param -> {
      JsonElement value = param.getValue().getValue().getValue();
      String stringValue = "";
      if (value.isJsonNull()) {
        stringValue = "Object/Array/Value";
      } else if (value.isJsonObject()) {
        stringValue = "Object";
      } else if (value.isJsonArray()) {
        stringValue = "Array [" + value.getAsJsonArray().size() + "]";
      } else if (value.getAsJsonPrimitive().isBoolean()) {
        stringValue = "Boolean";
      } else if (value.getAsJsonPrimitive().isString()) {
        stringValue = "String";
      } else if (value.getAsJsonPrimitive().isNumber()) {
        stringValue = "Number";
      }

      return new ReadOnlyStringWrapper(stringValue);
    });
    columnType.setCellFactory(param -> new TextFieldTreeTableCell<>());

    TreeTableColumn<Pair<String, JsonElement>, String> columnValue = new TreeTableColumn<>("VALUE");
    columnValue.setPrefWidth(300);
    columnValue.setCellValueFactory(param -> {
      JsonElement value = param.getValue().getValue().getValue();
      if (value.isJsonObject() || value.isJsonArray()) return null;

      String stringValue;
      if (value.getAsJsonPrimitive().isString()) {
        stringValue = value.getAsString();
      } else {
        stringValue = param.getValue().getValue().getValue().toString();
      }

      return new ReadOnlyStringWrapper(stringValue);
    });
    columnValue.setCellFactory(param -> new TextFieldTreeTableCell<>());

    ArrayList<TreeTableColumn<Pair<String, JsonElement>, String>> columnList = new ArrayList<>();
    columnList.add(columnTree);
    columnList.add(columnType);
    columnList.add(columnValue);

    return columnList;
  }

  @Override
  public TreeItem<Pair<String, JsonElement>> parseCode(String code) {
    TreeItem<Pair<String, JsonElement>> rootItem = new TreeItem<>(new Pair<>("Record", parser.parse(code)));
    rootItem.getChildren().addAll(parse(rootItem.getValue().getValue()));
    rootItem.setExpanded(false);
    return rootItem;
  }

  private List<TreeItem<Pair<String, JsonElement>>> parse(JsonElement jsonValue) {
    List<TreeItem<Pair<String, JsonElement>>> children = new ArrayList<>();
    if (jsonValue.isJsonObject()) {
      for (Map.Entry<String, JsonElement> member : jsonValue.getAsJsonObject().entrySet()) {
        TreeItem<Pair<String, JsonElement>> child = new TreeItem<>(
          new Pair<>(member.getKey(), member.getValue()));

        child.getChildren().addAll(parse(child.getValue().getValue()));
        child.setExpanded(false);
        children.add(child);
      }
    } else if (jsonValue.isJsonArray()) {
      int index = 0;
      for (JsonElement value : jsonValue.getAsJsonArray()) {
        TreeItem<Pair<String, JsonElement>> child = new TreeItem<>(
          new Pair<>("[" + index + "]", value));

        child.getChildren().addAll(parse(value));
        child.setExpanded(false);
        children.add(child);
        index++;
      }
    }

    return children;
  }

}
