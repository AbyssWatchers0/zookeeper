package com.abyss.case1;

import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * @author Abyss Watchers
 * @create 2022-12-20 10:38
 */
public class DistributeServer {
    public static void main(String[] args) throws Exception {
        // 获取zk连接
        ZooKeeper zk = new ZooKeeper("192.168.10.128:2181,192.168.10.130:2181", 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {

            }
        });

        // 注册服务器到zk集群
        String server1 = zk.create("/servers/server1", "192.168.10.128".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        // 启动业务逻辑
        Thread.sleep(Long.MAX_VALUE);
    }
}
