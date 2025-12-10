package com.pm.billingservice.config;

import net.javacrumbs.shedlock.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author Achintha Kalunayaka
 * @since 11/8/2025
 */

@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger("SchedulerLock");


    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }


    @Bean
    public LockingTaskExecutor loggingLockingTaskExecutor(LockProvider lockProvider) {
        return new LockingTaskExecutor() {
            private final DefaultLockingTaskExecutor delegate = new DefaultLockingTaskExecutor(lockProvider);

            @Override
            public void executeWithLock(Runnable task, LockConfiguration lockConfig) {

            }

            @Override
            public void executeWithLock(Task task, LockConfiguration lockConfig) throws Throwable {
                Optional<SimpleLock> lock = lockProvider.lock(lockConfig);
                if (lock.isEmpty()) {
                    logger.info("🔒 Another instance holds lock '{}', skipping execution at {}",
                            lockConfig.getName(), LocalDateTime.now());
                    return;
                }

                try {
                    logger.info("✅ Acquired lock '{}', executing task at {}",
                            lockConfig.getName(), LocalDateTime.now());
                    delegate.executeWithLock(task, lockConfig);
                } finally {
                    lock.ifPresent(l -> {
                        l.unlock();
                        logger.info("🔓 Released lock '{}' at {}", lockConfig.getName(), LocalDateTime.now());
                    });
                }
            }

            @Override
            public <T> TaskResult<T> executeWithLock(TaskWithResult<T> task, LockConfiguration lockConfig) throws Throwable {
                return LockingTaskExecutor.super.executeWithLock(task, lockConfig);
            }

        };
    }
}
