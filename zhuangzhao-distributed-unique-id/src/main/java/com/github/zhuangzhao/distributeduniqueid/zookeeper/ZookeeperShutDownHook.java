package com.github.zhuangzhao.distributeduniqueid.zookeeper;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.log4j.Logger;

/**
 * @author zhuangzhao.zhu, <zhuangzhao2726@gmail.com>
 * @date 2019-05-26.
 */
@Data
public class ZookeeperShutDownHook extends Thread {

  Logger logger = Logger.getLogger(ZookeeperShutDownHook.class);

  private String zookeeperServerList;

  private ZookeeperNode zookeeperNode;

  @Override
  public void run() {
    CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperServerList, new RetryNTimes(10, 5000));
    client.start();
    try {
      client.delete().forPath(zookeeperNode.getZkPath());
      logger.info("清除zookeeper上的永久节点：" + zookeeperNode.getZkPath());
    } catch (Exception e) {
      logger.error("清除zookeeper上的永久节点：" + zookeeperNode.getZkPath() + "时出现异常.");
      e.printStackTrace();
    } finally {
      client.close();
    }
  }
}
