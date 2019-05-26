package com.github.zhuangzhao.distributeduniqueid.exception;

/**
 * @author zhuangzhao.zhu, <zhuangzhao2726@gmail.com>
 * @date 2019-05-26.
 */
public class ZookeeperNodeNotExistException extends RuntimeException {

  public ZookeeperNodeNotExistException() {
    super();
  }

  public ZookeeperNodeNotExistException(String message) {
    super(message);
  }
}
