package io.sj.streaming.kinesis.consumer.parsing;

import java.util.List;

/**
 * Parser definition to parse the raw data and return metadata
 * @param <DATA>
 * @param <METADATA>
 */
public interface Parser<DATA, METADATA> {

  /**
   * Parse a String into
   *
   * @param code The code to parse, as a string
   * @return The root TreeItem than describe the parsed code
   * @throws Exception If an error occur during parsing
   */
  DATA parse(String code);

  /**
   * @return A list of column to display in the TreeTableView next to the editor
   */
  List<METADATA> getMetadata();

}
