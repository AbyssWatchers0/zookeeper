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
                System.out.println("1��ȡ��");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                lock1.zkUnlock();
                System.out.println("1�ͷ���");
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                lock2.zkLock();
                System.out.println("2��ȡ��");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                lock2.zkUnlock();
                System.out.println("2�ͷ���");
            }
        }).start();
    }
}
