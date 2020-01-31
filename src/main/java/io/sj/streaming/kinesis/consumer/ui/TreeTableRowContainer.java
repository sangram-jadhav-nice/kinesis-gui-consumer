package io.sj.streaming.kinesis.consumer.ui;

import javafx.scene.control.*;
import javafx.util.Pair;

import java.util.ResourceBundle;

public class TreeTableRowContainer<T> extends TreeTableRow<Pair<String, T>> {
    private final ContextMenu mContextMenu;

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

        contextMenu.getItems().addAll(expandAllItems, collapseAllItems,
                expandAllChildrenItems, collapseAllChildrenItems);

        return contextMenu;
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
