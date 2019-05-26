package com.github.zhuangzhao.distributeduniqueid.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author zhuangzhao.zhu, <zhuangzhao2726@gmail.com>
 * @date 2019-05-25.
 */
@Component
@Getter
@Setter
public class AppConfig {

  @Value("${app.name:-}")
  public String appName;


  @Value("${distributed.app.name:-}")
  public String distributedAppName;


  @Value("${distributed.machineroom.name}")
  public String machineRoomName;

}
