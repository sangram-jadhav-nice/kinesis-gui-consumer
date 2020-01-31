package io.sj.streaming.kinesis.consumer;

public interface OnCancelListener {
    void setOnCancelled(Runnable runnable);
}