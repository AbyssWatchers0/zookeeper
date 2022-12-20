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
            // 获取连接
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
            // 等待建立连接
            connectLatch.await();

            // 如果没有根节点就创建
            Stat stat = zk.exists("/locks", false);
            if (stat == null) {
                zk.create("/locks", "locks".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加锁
     */
    public void zkLock() {
        try {
            // 创建临时节点
            curNode = zk.create("/locks/seq-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("node " + curNode + " created");
            // 获取/locks下的子节点
            List<String> children = zk.getChildren("/locks", false);

            System.out.println("children count=" +children.size());
            if (children.size() == 1) {
                // 如果只有一个节点，说明当前节点排第一位，可以获取锁
                return;
            } else if (children.size() <= 0) {
                System.out.println("数据异常");
            } else {
                // 如果有多个节点，判断当前节点是否排第一
                // 对根节点下的所有子节点从小到大排序
                Collections.sort(children);
                int i = children.indexOf(curNode.substring("/locks/".length()));
                System.out.println("current node index = " + i);
                if (i == -1) {
                    System.out.println("数据异常");
                } else if(i == 0) {
                    // 当前节点排第一位，可以获取锁
                    return;
                } else {
                    // 监听当前节点的前一位节点
                    String preNode = children.get(i - 1);
                    waitPath = "/locks/" + preNode;
                    System.out.println("waitPath:" + waitPath);
                    zk.getData(waitPath, true, null);
                    // 等待锁
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
     * 解锁
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
