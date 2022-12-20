package com.abyss.case2;

/**
 * @author Abyss Watchers
 * @create 2022-12-20 13:09
 */
public class DistributeLockTest {
    public static void main(String[] args) {
        DistributeLock lock1 = new DistributeLock();
        DistributeLock lock2 = new DistributeLock();
        new Thread(new Runnable() {
            @Override
            public void run() {
                lock1.zkLock();
                System.out.println("1获取锁");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                lock1.zkUnlock();
                System.out.println("1释放锁");
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                lock2.zkLock();
                System.out.println("2获取锁");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                lock2.zkUnlock();
                System.out.println("2释放锁");
            }
        }).start();
    }
}
