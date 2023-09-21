package com.whl.redission;

import java.time.Duration;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class TestRedission {

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.1.243:6379").setDatabase(0).setPassword("em7_redis");
        RedissonClient redisson = Redisson.create(config);
        RBucket<Object> bucket = redisson.getBucket("test");
        Object oldValue = bucket.get();

        if (oldValue != null) {
            System.out.println("oldValue=" + oldValue);
        } else {
            bucket.set("value", Duration.ofSeconds(20));
            System.out.println(redisson.getBucket("test").get());
        }
        RLock lock = redisson.getLock("test-lock");

        if (lock.tryLock()) {
            Thread.sleep(10000L);
            redisson.getBucket("other").set("other-value", Duration.ofSeconds(20));
        }
        lock.unlock();
        redisson.shutdown();
    }

}
