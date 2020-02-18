package io.sj.streaming.kinesis.consumer.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TreeTableRowContainer<T> extends TreeTableRow<Pair<String, T>> {
  private final ContextMenu mContextMenu;
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public TreeTableRowContainer() {
    mContextMenu = createContextMenu();
  }

  @Override
  protected void updateItem(Pair<String, T> item, boolean empty) {
    super.updateItem(item, empty);
    TreeItem treeItem = getTreeItem();
    if (treeItem != null && treeItem.getChildren().isEmpty()) {
      setContextMenu(null);
    } else {
      setContextMenu(mContextMenu);
    }
  }

  private ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem expandAllItems = new MenuItem("Expand All");
    MenuItem collapseAllItems = new MenuItem("Collapse All");
    MenuItem expandAllChildrenItems = new MenuItem("Expand All Children");
    MenuItem collapseAllChildrenItems = new MenuItem("Collapse All Children");
    MenuItem separator = new SeparatorMenuItem();
    MenuItem copyItem = new MenuItem("Copy Selected");
    MenuItem copyAllItems = new MenuItem("Copy All");

    expandAllItems.setOnAction(event -> {
      TreeItem<Pair<String, T>> item = getTreeItem();
      item.setExpanded(true);
      expandAllChildren(item, true);
      event.consume();
    });
    collapseAllItems.setOnAction(event -> {
      TreeItem<Pair<String, T>> item = getTreeItem();
      item.setExpanded(false);
      expandAllChildren(item, false);
      event.consume();
    });
    expandAllChildrenItems.setOnAction(event -> {
      TreeItem<Pair<String, T>> item = getTreeItem();
      expandAllChildren(item, true);
      event.consume();
    });
    collapseAllChildrenItems.setOnAction(event -> {
      TreeItem<Pair<String, T>> item = getTreeItem();
      expandAllChildren(item, false);
      event.consume();
    });

    copyItem.setOnAction(event -> {
      TreeItem<Pair<String, T>> item = getTreeItem();
      final ClipboardContent clipboardContent = new ClipboardContent();
      clipboardContent.putString(item.getValue().getValue().toString());
      Clipboard.getSystemClipboard().setContent(clipboardContent);
      event.consume();
    });

    copyAllItems.setOnAction(event -> {
      final ClipboardContent clipboardContent = new ClipboardContent();
      clipboardContent.putString(processTable(this.getTreeTableView().getRoot().getChildren()).toString());
      Clipboard.getSystemClipboard().setContent(clipboardContent);
      event.consume();
    });

    contextMenu.getItems().addAll(expandAllItems, collapseAllItems,
      expandAllChildrenItems, collapseAllChildrenItems,
      separator, copyItem, copyAllItems);

    return contextMenu;
  }

  private List<String> processTable(ObservableList<TreeItem<Pair<String, T>>> tableDatum) {
    List<String> jsonData = new ArrayList<>();
    tableDatum.forEach(data -> jsonData.add(gson.toJson(data.getValue().getValue())));
    return jsonData;
  }

  private static <T> void expandAllChildren(TreeItem<Pair<String, T>> item, boolean expand) {
    item.getChildren().forEach(o -> {
      if (!o.getChildren().isEmpty()) {
        o.setExpanded(expand);
        expandAllChildren(o, expand);
      } else {
        o.setExpanded(!expand);
      }
    });
  }

}
