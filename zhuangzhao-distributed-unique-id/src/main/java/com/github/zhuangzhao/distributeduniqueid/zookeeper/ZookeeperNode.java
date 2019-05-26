package com.github.zhuangzhao.distributeduniqueid.zookeeper;

import lombok.Data;

import java.io.File;

/**
 * @author zhuangzhao.zhu, <zhuangzhao2726@gmail.com>
 * @date 2019-05-26.
 */
@Data
public class ZookeeperNode {

  private String nameSpace;

  private String relativePath;

  private String zkPath;



  public ZookeeperNode() {
  }

  public ZookeeperNode(String nameSpace, String separator, String relativePath) {
    this.nameSpace = nameSpace;
    this.relativePath = relativePath;
    this.zkPath = nameSpace + separator + relativePath;
  }
}
