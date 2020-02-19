package io.sj.streaming.kinesis.consumer.kinesis;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.*;
import akka.stream.alpakka.kinesis.ShardSettings;
import akka.stream.alpakka.kinesis.javadsl.KinesisSource;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.amazonaws.client.builder.ExecutorFactory;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import com.amazonaws.services.kinesis.clientlibrary.types.UserRecord;
import com.amazonaws.services.kinesis.clientlibrary.utils.NamedThreadFactory;
import com.amazonaws.services.kinesis.model.ListShardsRequest;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.kinesis.model.Shard;
import com.amazonaws.services.kinesis.model.ShardIteratorType;
import com.google.gson.JsonElement;
import io.sj.streaming.kinesis.consumer.OnCancelListener;
import io.sj.streaming.kinesis.consumer.parsing.JsonParser;
import io.sj.streaming.kinesis.consumer.parsing.RecordProcessorDelegate;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

public class KinesisConsumer implements OnCancelListener {

  private final Logger LOG = LoggerFactory.getLogger(KinesisConsumer.class);

  private String stream;
  private String schema;
  private RecordProcessorDelegate type;
  private ObservableList<TreeItem<Pair<String, JsonElement>>> observableList;
  private Runnable onCancel;
  private JsonParser parser = new JsonParser();
  private ActorSystem system;
  private ActorMaterializer materializer;
  private akka.japi.Pair<UniqueKillSwitch, CompletionStage<Done>> streamFlow;
  private ExecutorService executorService;
  private ExecutorFactory executorFactory;

  public KinesisConsumer(ObservableList<TreeItem<Pair<String, JsonElement>>> observableList) {
    this.observableList = observableList;
    ThreadFactory threadFactory = new NamedThreadFactory("kinesis-consumer-");
    this.executorService = Executors.newFixedThreadPool(5, threadFactory);
    this.executorFactory = () -> executorService;
    this.system = ActorSystem.create();
    akka.japi.function.Function<Throwable, Supervision.Directive> exceptionHandler = exc -> {
      LOG.error("Error occurred while processing record", exc);
      return Supervision.resume();
    };
    this.materializer = ActorMaterializer.create(
      ActorMaterializerSettings.create(system)
        .withSupervisionStrategy(exceptionHandler), system);
  }

  public KinesisConsumer withStream(String stream) {
    this.stream = stream;
    return this;
  }

  public KinesisConsumer withType(RecordProcessorDelegate type) {
    this.type = type;
    return this;
  }

  public KinesisConsumer withSchema(String schema) {
    this.schema = schema;
    return this;
  }

  public void stopConsumer() {
    this.streamFlow.first().shutdown();
    this.executorService.shutdown();
    materializer.shutdown();
    system.terminate();
  }

  public void runConsumer() {
    this.streamFlow = runFlow(stream);
  }

  private akka.japi.Pair<UniqueKillSwitch, CompletionStage<Done>> runFlow(String stream) {
    Source<Record, NotUsed> source = buildKinesisSource(stream);
    Flow<Record, TreeItem<Pair<String, JsonElement>>, NotUsed> flow = Flow.of(Record.class).map(v -> {
      try {
        Record[] list = new Record[1];
        list[0] = v;
        List<UserRecord> records = UserRecord.deaggregate(Arrays.asList(list));
        return records.stream()
          .map(rec -> type.getProcessor(schema).apply(rec))
          .filter(Objects::nonNull)
          .map(x -> parser.parse(x))
          .collect(Collectors.toList());
      } catch (Exception e) {
        LOG.error("Invalid record received: {}", v.getData().rewind().array(), e);
        throw e;
      }
    }).mapConcat(f -> f);

    return source.via(flow)
      .viaMat(KillSwitches.single(), Keep.right())
      .toMat(Sink.foreach(x -> Platform.runLater(() -> observableList.add(x))), Keep.both()).run(materializer);
  }

  private Source<Record, NotUsed> buildKinesisSource(String stream) {
    final AmazonKinesisAsync client = AmazonKinesisAsyncClientBuilder.standard()
      .withExecutorFactory(executorFactory).build();
    ListShardsRequest request = new ListShardsRequest().withStreamName(stream);
    List<Shard> shards = client.listShards(request).getShards();
    List<ShardSettings> shardSettings = new ArrayList<>();
    shards.forEach(shard -> {
      final ShardSettings setting =
        ShardSettings.create(stream, shard.getShardId())
          .withRefreshInterval(Duration.ofMillis(250))
          .withLimit(500)
          .withShardIteratorType(ShardIteratorType.LATEST);

      shardSettings.add(setting);
    });
    return KinesisSource.basicMerge(shardSettings, client);
  }

  private void runOnCancel() {
    Platform.runLater(onCancel);
  }

  @Override
  public void setOnCancelled(Runnable runnable) {
    onCancel = runnable;
  }
}
