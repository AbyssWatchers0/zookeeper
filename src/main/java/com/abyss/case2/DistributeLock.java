package com.abyss.case2;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Abyss Watchers
 * @create 2022-12-20 12:09
 */
public class DistributeLock {
    private ZooKeeper zk;
    private String waitPath;
    private String curNode;
    private CountDownLatch connectLatch = new CountDownLatch(1);
    private CountDownLatch waitLatch = new CountDownLatch(1);

    public DistributeLock() {
        try {
            // ��ȡ����
            zk = new ZooKeeper("192.168.10.128:2181,192.168.10.130:2181", 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        connectLatch.countDown();
                    }

                    if (event.getType() == Event.EventType.NodeDeleted && event.getPath().equals(waitPath)) {
                        System.out.println("waitLatch release");
                        waitLatch.countDown();
                    }
                }
            });
            // �ȴ���������
            connectLatch.await();

            // ���û�и��ڵ�ʹ���
            Stat stat = zk.exists("/locks", false);
            if (stat == null) {
                zk.create("/locks", "locks".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ����
     */
    public void zkLock() {
        try {
            // ������ʱ�ڵ�
            curNode = zk.create("/locks/seq-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("node " + curNode + " created");
            // ��ȡ/locks�µ��ӽڵ�
            List<String> children = zk.getChildren("/locks", false);

            System.out.println("children count=" +children.size());
            if (children.size() == 1) {
                // ���ֻ��һ���ڵ㣬˵����ǰ�ڵ��ŵ�һλ�����Ի�ȡ��
                return;
            } else if (children.size() <= 0) {
                System.out.println("�����쳣");
            } else {
                // ����ж���ڵ㣬�жϵ�ǰ�ڵ��Ƿ��ŵ�һ
                // �Ը��ڵ��µ������ӽڵ��С��������
                Collections.sort(children);
                int i = children.indexOf(curNode.substring("/locks/".length()));
                System.out.println("current node index = " + i);
                if (i == -1) {
                    System.out.println("�����쳣");
                } else if(i == 0) {
                    // ��ǰ�ڵ��ŵ�һλ�����Ի�ȡ��
                    return;
                } else {
                    // ������ǰ�ڵ��ǰһλ�ڵ�
                    String preNode = children.get(i - 1);
                    waitPath = "/locks/" + preNode;
                    System.out.println("waitPath:" + waitPath);
                    zk.getData(waitPath, true, null);
                    // �ȴ���
                    System.out.println(Thread.currentThread().getName() + "wait release waitLatch");
                    waitLatch.await();
                    System.out.println(Thread.currentThread().getName() + "get lock");

                }
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * ����
     */
    public void zkUnlock() {
        try {
            System.out.println("unlock:" + curNode);
            zk.delete(curNode, -1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}
