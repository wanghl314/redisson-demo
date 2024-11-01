package com.whl.redisson;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TestRedisson {
    private static Logger logger = LoggerFactory.getLogger(TestRedisson.class);

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.1.243:6379").setPassword("em7_redis").setDatabase(0);
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

        Thread[] threads = new Thread[20];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Task(i, lock, redisson));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        redisson.shutdown();
    }

    private static class Task implements Runnable {
        private final int index;

        private final RLock lock;

        private final RedissonClient redisson;

        public Task(int index, RLock lock, RedissonClient redisson) {
            this.index = index;
            this.lock = lock;
            this.redisson = redisson;
        }

        @Override
        public void run() {
            switch (this.index % 4) {
                case 0:
                    this.test1();
                    break;
                case 1:
                    this.test2();
                    break;
                case 2:
                    this.test3();
                    break;
                case 3:
                    this.test4();
                    break;
                default:
            }
        }

        /**
         * 阻塞获取锁
         */
        private void test1() {
            try {
                long start = System.currentTimeMillis();
                this.lock.lock();
                System.out.println("task-" + this.index + " lock acquired");
                Thread.sleep(new Random().nextLong(10000L));
                this.redisson.getBucket("other").set("other-value", Duration.ofSeconds(20));
                long end = System.currentTimeMillis();
                System.out.println("task-" + this.index + " run " + (end - start) + " ms");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                this.lock.unlock();
            }
        }

        /**
         * 非阻塞获取锁，获取不到立即返回
         */
        private void test2() {
            boolean acquire = false;

            try {
                long start = System.currentTimeMillis();
                acquire = this.lock.tryLock();

                if (acquire) {
                    System.out.println("task-" + this.index + " lock acquired");
                    Thread.sleep(new Random().nextLong(10000L));
                    this.redisson.getBucket("other").set("other-value", Duration.ofSeconds(20));
                }
                long end = System.currentTimeMillis();
                System.out.println("task-" + this.index + " run " + (end - start) + " ms");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (acquire) {
                    this.lock.unlock();
                }
            }
        }

        /**
         * 非阻塞获取锁，获取不到等2秒
         * <ul>
         *   <li>获取到锁，执行业务逻辑，锁自动续期</li>
         *   <li>获取到锁立即返回</li>
         * </ul>
         */
        private void test3() {
            boolean acquire = false;

            try {
                long start = System.currentTimeMillis();
                acquire = this.lock.tryLock(3, TimeUnit.SECONDS);

                if (acquire) {
                    System.out.println("task-" + this.index + " lock acquired");
                    Thread.sleep(new Random().nextLong(10000L));
                    this.redisson.getBucket("other").set("other-value", Duration.ofSeconds(20));
                }
                long end = System.currentTimeMillis();
                System.out.println("task-" + this.index + " run " + (end - start) + " ms");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (acquire) {
                    this.lock.unlock();
                }
            }
        }

        /**
         * 非阻塞获取锁，获取不到等2秒
         * <ul>
         *   <li>获取到锁，执行业务逻辑，锁只占用3秒</li>
         *   <li>获取到锁立即返回</li>
         * </ul>
         */
        private void test4() {
            boolean acquire = false;

            try {
                long start = System.currentTimeMillis();
                acquire = this.lock.tryLock(3, 5,  TimeUnit.SECONDS);

                if (acquire) {
                    System.out.println("task-" + this.index + " lock acquired");
                    Thread.sleep(new Random().nextLong(10000L));
                    this.redisson.getBucket("other").set("other-value", Duration.ofSeconds(20));
                }
                long end = System.currentTimeMillis();
                System.out.println("task-" + this.index + " run " + (end - start) + " ms");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (acquire) {
                    this.lock.unlock();
                }
            }
        }

    }

}
