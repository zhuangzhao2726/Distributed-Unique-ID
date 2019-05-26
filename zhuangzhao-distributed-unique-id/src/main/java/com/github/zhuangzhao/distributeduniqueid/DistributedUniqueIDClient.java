package com.github.zhuangzhao.distributeduniqueid;

import com.github.zhuangzhao.distributeduniqueid.config.AppConfig;
import com.github.zhuangzhao.distributeduniqueid.snowflake.SnowflakeIdWorker;
import com.github.zhuangzhao.distributeduniqueid.exception.DistrobutedUniqueIDInvalidPropertyException;
import com.github.zhuangzhao.distributeduniqueid.zookeeper.DistributedUniqueIDZKClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * @author zhuangzhao.zhu, <zhuangzhao2726@gmail.com>
 * @date 2019-05-25.
 */
@Component
public class DistributedUniqueIDClient {

  Logger logger = Logger.getLogger(DistributedUniqueIDClient.class);

  private volatile boolean initialized = false;

  private static final String DEFAULT_NAME = "-";

  @Autowired
  AppConfig appConfig;

  @Autowired
  DistributedUniqueIDZKClient zkClient;

  private String machineRoomName;

  private String appName;

  private Long machineRoomId;

  private Long workId;

  private SnowflakeIdWorker snowflakeIdWorker;


  public Long genId() {
      if(!initialized || snowflakeIdWorker == null) {
        initlize();
      }
      return snowflakeIdWorker.nextId();
  }

  @PostConstruct
  private void initlize() {

    String macineRoomName = appConfig.getMachineRoomName();
    String appName = appConfig.getAppName();
    String distributedAppName = appConfig.getDistributedAppName();

    checkPropertyVaild(macineRoomName, distributedAppName, appName);

    this.machineRoomId = zkClient.getmachineRoomId(this.machineRoomName);
    this.workId = zkClient.getWorkerId(this.machineRoomName, this.appName);

    if(this.machineRoomId < 0 || this.machineRoomId >= 32) {
      logger.error("machineRoomId : " + this.machineRoomId + " 无效.");
      throw new DistrobutedUniqueIDInvalidPropertyException("machineRoomId : " + this.machineRoomId + " 无效.");
    }
    if (this.workId < 0 || this.workId >= 32) {
      logger.error("workId : " + this.workId + " 无效.");
      throw new DistrobutedUniqueIDInvalidPropertyException("workId : " + this.workId + " 无效.");
    }

    snowflakeIdWorker = new SnowflakeIdWorker(this.machineRoomId, this.workId);
    logger.info("distributed unique id 初始化完成：machineRoomId = " + this.machineRoomId + ", workId = " + workId);
    initialized = true;
  }

  private void checkPropertyVaild(String macineRoomName, String distributedAppName, String appName) {

    if(StringUtils.isEmpty(macineRoomName)) {
      logger.error("配置项：[distributed.machineroom.name] 的值:[" + macineRoomName + "]无效");
      throw new DistrobutedUniqueIDInvalidPropertyException("配置项：[distributed.machineroom.name] 的值:[" + macineRoomName + "]无效");
    }
    this.machineRoomName = macineRoomName;
    if(StringUtils.isEmpty(distributedAppName) || DEFAULT_NAME.equalsIgnoreCase(distributedAppName)) {
      logger.warn("配置项：[distributed.app.name] 的值:[" + distributedAppName + "]无效");
      if(StringUtils.isEmpty(appName) || DEFAULT_NAME.equalsIgnoreCase(appName)) {
        logger.error("配置项：[app.name] 的值:[" + appName + "]无效");
        throw new DistrobutedUniqueIDInvalidPropertyException("配置项：[app.name] 的值:[" + appName + "]无效");
      } else {
        this.appName = appName;
      }
    } else {
      this.appName = distributedAppName;
    }
  }

}
