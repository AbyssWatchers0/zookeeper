package com.abyss.zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Abyss Watchers
 * @create 2022-12-20 9:35
 */
public class ZkClient {
    private static final String CONNECTSTRING = "192.168.10.128:2181,192.168.10.130:2181";
    private ZooKeeper zkClient = null;

    @BeforeEach
    public void init() throws Exception{
        zkClient = new ZooKeeper(CONNECTSTRING, 20000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println(watchedEvent);
            }
        });
    }

    /**
     * 创建节点
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void create() throws KeeperException, InterruptedException {
        String nodeCreated = zkClient.create("/hello", "hello zk".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(nodeCreated);
    }

    /**
     * 监听节点增减变化
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void getChildren() throws KeeperException, InterruptedException {
        // 传true，监听器为zkClient初始化时传入的Watcher
        List<String> children = zkClient.getChildren("/hello", false);
        for (String child : children) {
            System.out.println(child);
        }

        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 监听节点数据的变化
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void getData() throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        byte[] data = zkClient.getData("/hello", new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println(watchedEvent);
            }
        }, stat);
        System.out.println(new String(data) + ",stat:" + stat);
    }

    /**
     * 判断Znode是否存在
     */
    @Test
    public void exist() throws KeeperException, InterruptedException {
        Stat stat = zkClient.exists("/hello", false);
        System.out.println(stat == null ? "not exist" : "exist");
        System.out.println(stat);
    }
}
