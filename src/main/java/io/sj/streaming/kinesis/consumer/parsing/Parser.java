package io.sj.streaming.kinesis.consumer.parsing;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Pair;

import java.util.List;

public interface Parser<S> {

  /**
   * Parse a String
   *
   * @param code The code to parse, as a string
   * @return The root TreeItem than describe the parsed code
   * @throws Exception If an error occur during parsing
   */
  TreeItem<Pair<String, S>> parseCode(String code);

  /**
   * @return A list of column to display in the TreeTableView next to the editor
   */
  List<TreeTableColumn<Pair<String, S>, String>> getColumns();

}
