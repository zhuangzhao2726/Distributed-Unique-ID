package com.github.zhuangzhao.distributeduniqueid;

import com.github.zhuangzhao.distributeduniqueid.snowflake.SnowflakeIdWorker;
import org.junit.Test;

/**
 * @author zhuangzhao.zhu, <zhuangzhao2726@gmail.com>
 * @date 2019-05-26.
 */
public class SnowflakeIdWorkerTest {


  @Test
  public void nextId() {

    SnowflakeIdWorker worker = new SnowflakeIdWorker(1,1);
    for(int i = 0; i < 10; i++){
      Long id = worker.nextId();
      System.out.println(id);
    }
  }

}
