package io.sj.streaming.kinesis.consumer.parsing;

import com.amazonaws.services.kinesis.clientlibrary.types.UserRecord;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Function;

public enum RecordProcessorDelegate {
  JSON {
    @Override
    public Function<UserRecord, String> getProcessor(String schema) {
      return (rec) -> {
        byte[] bytes = rec.getData().array();
        return new String(bytes);
      };
    }
  },
  AVRO {
    @Override
    public Function<UserRecord, String> getProcessor(String schema) {
      return (rec) -> {
        byte[] bytes = rec.getData().array();
        GenericDatumReader<GenericRecord> datumReader = new GenericDatumReader<>(new org.apache.avro.Schema.Parser().parse(schema));
        SeekableByteArrayInput inputStream = new SeekableByteArrayInput(bytes);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(inputStream, null);
        try {
          return datumReader.read(null, decoder).toString();
        } catch (IOException ioe) {
          LOG.error("Invalid record", ioe);
          return null;
        }
      };
    }
  };

  public abstract Function<UserRecord, String> getProcessor(String schema);

  private static final Logger LOG = LoggerFactory.getLogger(RecordProcessorDelegate.class);
}

