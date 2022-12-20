package com.abyss.case3;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author Abyss Watchers
 * @create 2022-12-20 14:25
 */
public class CuratorLockTest {
    public static void main(String[] args) {
        // 创建分布式锁1
        InterProcessMutex lock1 = new InterProcessMutex(getCuratorFramework(), "/locks");

        // 创建分布式锁2
        InterProcessMutex lock2 = new InterProcessMutex(getCuratorFramework(), "/locks");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock1.acquire();
                    System.out.println("thread1 get lock");

                    lock1.acquire();
                    System.out.println("thread1 get lock again");

                    Thread.sleep(3*1000);

                    lock1.release();
                    System.out.println("thread1 release lock");
                    lock1.release();
                    System.out.println("thread1 release lock again");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock2.acquire();
                    System.out.println("thread2 get lock");

                    lock2.acquire();
                    System.out.println("thread2 get lock again");

                    Thread.sleep(2*1000);

                    lock2.release();
                    System.out.println("thread2 release lock");
                    lock2.release();
                    System.out.println("thread2 release lock again");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static CuratorFramework getCuratorFramework() {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(4000, 3);

        CuratorFramework client = CuratorFrameworkFactory.builder().connectString("192.168.10.128:2181,192.168.10.130:2181")
                .connectionTimeoutMs(4000)
                .sessionTimeoutMs(4000)
                .retryPolicy(retryPolicy)
                .build();

        // 启动客户端
        client.start();
        System.out.println("zookeeper 启动成功");

        return client;
    }
}
