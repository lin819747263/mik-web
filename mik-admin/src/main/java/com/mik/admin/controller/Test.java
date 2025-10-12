package com.mik.admin.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Test {
    private int num = 0;
    private ExecutorService executorService = Executors.newFixedThreadPool(500);
    private Lock lock = new ReentrantLock();



    public void testSync(){
        for (int i = 0; i < 1000000; i++) {
            executorService.submit(() -> {
                synchronized (this) {
                    num = num + 1;
                }
            });
        }
        System.out.println(num);
    }

    public void testLock(){
        for (int i = 0; i < 1000000; i++) {
            executorService.submit(() -> {
                lock.lock();
                num = num + 1;
                lock.unlock();
            });
        }
        System.out.println(num);
    }


    public static void main(String[] args) {
        Test test = new Test();
        long start = System.currentTimeMillis();
//        test.testSync();
        test.testLock();
        long end = System.currentTimeMillis();
        System.out.println("用时" + (end - start) + "ms");
    }



}
