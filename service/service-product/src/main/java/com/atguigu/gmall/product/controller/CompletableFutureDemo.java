package com.atguigu.gmall.product.controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Hobo
 * @create 2021-02-09 21:48
 */

public class CompletableFutureDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        //创建异步对象
//        CompletableFuture<Integer> exceptionally = CompletableFuture.supplyAsync(() -> {
//            System.out.println(Thread.currentThread().getName() + "\t completableFuture");
//            int err = 1 / 0;
//            return 1024;
//        }).whenComplete(new BiConsumer<Integer, Throwable>() {
//            @Override
//            public void accept(Integer integer, Throwable throwable) {
//                System.out.println("Throwable:" + throwable);
//                int i = integer * 2;
//                System.out.println(i);
//            }
//        }).exceptionally(new Function<Throwable, Integer>() {
//            @Override
//            public Integer apply(Throwable throwable) {
//                System.out.println("throwable=" + throwable);
//                return 6666;
//            }
//        });
//        System.out.println(exceptionally.get());


        //并行化线程
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                return "hello";
            }
        });

        CompletableFuture<Void> futureB = futureA.thenAcceptAsync(new Consumer<String>() {
            @Override
            public void accept(String s) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(s + "word");
            }
        });

        CompletableFuture<Void> futureC = futureA.thenAcceptAsync(new Consumer<String>() {
            @Override
            public void accept(String s) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(s + "java");
            }
        });


    }
}
