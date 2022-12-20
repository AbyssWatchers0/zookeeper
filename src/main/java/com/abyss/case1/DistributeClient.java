package com.abyss.case1;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Abyss Watchers
 * @create 2022-12-20 10:47
 */
public class DistributeClient {
    private static ZooKeeper zk;
    public static void main(String[] args) throws Exception {
        // 获取zk连接
        zk = new ZooKeeper("192.168.10.128:2181,192.168.10.130:2181", 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                watchServers();
            }
        });


        // 监听/servers 下的子节点的增减变化
        watchServers();

        // 业务逻辑
        Thread.sleep(Long.MAX_VALUE);
    }

    private static void watchServers() {
        try {
            ArrayList<String> servers = new ArrayList<>();
            List<String> children = zk.getChildren("/servers", true);
            for (String child : children) {
                byte[] data = zk.getData("/servers/" + child, false, new Stat());
                servers.add(new String(data));
            }

            System.out.println(servers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
