package com.yuhtin.quotes.bot.thumbnail.util;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

public class Promise<T> {

    private final FutureTask<T> future;

    private Promise(FutureTask<T> future) {
        this.future = future;
    }

    public static <U> Promise<U> supply(Callable<U> task) {
        FutureTask<U> future = new FutureTask<>(task);
        return new Promise<>(future);
    }

    public void then(Consumer<T> callback) {
        if (future != null) {
            new Thread(() -> {
                try {
                    future.run();

                    T result = future.get();
                    callback.accept(result);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

}