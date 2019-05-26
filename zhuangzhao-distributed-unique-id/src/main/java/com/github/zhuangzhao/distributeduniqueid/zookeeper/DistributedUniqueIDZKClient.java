package com.github.zhuangzhao.distributeduniqueid.zookeeper;

import com.github.zhuangzhao.distributeduniqueid.exception.ZookeeperNodeNotExistException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.RetryNTimes;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;

/**
 * @author zhuangzhao.zhu, <zhuangzhao2726@gmail.com>
 * @date 2019-05-26.
 */
@Component
public class DistributedUniqueIDZKClient {

  Logger logger = Logger.getLogger(DistributedUniqueIDZKClient.class);

  @Value("${distributed.unique.id.zkServerList}")
  public String zkServerList;

  private static final String NAME_SPACE = "/DistributedUniqueID";

  private static final int MAX_MACHINE_ROOM = 32;

  private static final int MAX_WORKER = 32;

  private static final String SEPARATOR = "/";

  private Long machineRoomId = -1L;

  private Long workerId = -1L;

  public Long getmachineRoomId(String macineRoomName) {

    ZookeeperNode zkNode = new ZookeeperNode(NAME_SPACE, SEPARATOR, macineRoomName);
    CuratorFramework client = CuratorFrameworkFactory.newClient(zkServerList, new RetryNTimes(10, 5000));
    client.start();
    try {
      if(client.checkExists().forPath(zkNode.getZkPath()) == null) {
        logger.warn("machine room : [" + macineRoomName + "] 不存在");
        throw new ZookeeperNodeNotExistException("machine room : [" + macineRoomName + "] 不存在");
      } else {
        byte[] machineRoomIdBytes = client.getData().forPath(zkNode.getZkPath());
        String machineRoomIdString = new String(machineRoomIdBytes);
        this.machineRoomId = Long.parseLong(machineRoomIdString);
      }
    } catch (ZookeeperNodeNotExistException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      client.close();
    }
    return this.machineRoomId;
  }

  public Long getWorkerId(String machineRoomName, String appName) {

    String relativeAppPath = machineRoomName + SEPARATOR + appName;
    ZookeeperNode zookeeperAppNode = new ZookeeperNode(NAME_SPACE, SEPARATOR, relativeAppPath);

    CuratorFramework client = CuratorFrameworkFactory.newClient(zkServerList, new RetryNTimes(10, 5000));
    client.start();
    try {
      if(client.checkExists().forPath(zookeeperAppNode.getZkPath()) == null){
        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zookeeperAppNode.getZkPath());
      }

      String zookeeperAppLatchPath = NAME_SPACE + SEPARATOR + relativeAppPath + SEPARATOR + "latch";
      executeInLeader(client, relativeAppPath, zookeeperAppLatchPath);

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      client.close();
    }

    return this.workerId;
  }


  /**
   * 在主节点执行操作.
   *  @param client
   * @param relativeAppPath
   * @param latchNode 分布式锁使用的作业节点名称
   */
  public void executeInLeader(CuratorFramework client, String relativeAppPath,
      final String latchNode) {
    try (LeaderLatch latch = new LeaderLatch(client, latchNode)) {
      latch.start();
      latch.await();
      executeSetWorkerID(client, relativeAppPath);
      //CHECKSTYLE:OFF
    } catch (final Exception ex) {
      //CHECKSTYLE:ON
      handleException(ex);
    }
  }

  private void executeSetWorkerID(CuratorFramework client, String relativeAppPath) {
    String presentIP = getServerIp();
    Long processID = getProcessId();
    String data = presentIP + "@#@" + processID;

    for(int i = 0; i < MAX_WORKER; i++) {
      String relativeWokerPath = relativeAppPath + SEPARATOR + ("worker" + i);
      ZookeeperNode zookeeperWorkerNode = new ZookeeperNode(NAME_SPACE, SEPARATOR, relativeWokerPath);
      try {
        if(client.checkExists().forPath(zookeeperWorkerNode.getZkPath()) == null){
          client.create().withMode(CreateMode.PERSISTENT).forPath(zookeeperWorkerNode.getZkPath(), data.getBytes());
          this.workerId = Long.valueOf(i);

          ZookeeperShutDownHook zookeeperShutDownHook = new ZookeeperShutDownHook();
          zookeeperShutDownHook.setZookeeperServerList(client.getZookeeperClient().getCurrentConnectionString());
          zookeeperShutDownHook.setZookeeperNode(zookeeperWorkerNode);
          Runtime.getRuntime().addShutdownHook(zookeeperShutDownHook);

          break;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 获取当前进程id
   */
  private Long getProcessId() {
    try {
      RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
      String name = runtime.getName();
      String pid = name.substring(0, name.indexOf('@' ));
      return Long.parseLong(pid);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 获取当前服务器ip地址
   */
  private String getServerIp() {
    try {
      //用 getLocalHost() 方法创建的InetAddress的对象
      InetAddress address = InetAddress.getLocalHost();
      return address.getHostAddress();
    } catch (Exception e) {
      return null;
    }
  }


  private void handleException(final Exception ex) {
    if (ex instanceof InterruptedException) {
      Thread.currentThread().interrupt();
    } else {
      throw new RuntimeException(ex);
    }
  }

}
