package com.bqy.user_center.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.user_center.model.domain.UserCenter;
import com.bqy.user_center.service.UserCenterService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserCenterService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    //重点用户
    List<Long> mainUserList = Arrays.asList(1798342677401206786L);

    //每天执行，加载预热推荐用户
    @Scheduled(cron = "0 30 15 * * *")
    public void doPreCacheRecommendUser() {
        RLock rLock = redissonClient.getLock("bqy:precachejob:docache:lock");
        try {
            if (rLock.tryLock(0, 30000L, TimeUnit.MILLISECONDS)) {
                System.out.println("getLock"+Thread.currentThread().getId());
                for (Long userId : mainUserList) {
                    QueryWrapper<UserCenter> queryWrapper = new QueryWrapper<>();
                    Page<UserCenter> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("bqy:user:recommend:%s", userId);
                    ValueOperations<String, Object> operations = redisTemplate.opsForValue();
                    //写缓存
                    try {
                        operations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }

            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error",e);
        } finally {
            //只能释放自己的锁
            if (rLock.isHeldByCurrentThread()) {
                System.out.println("unlock"+Thread.currentThread().getId());
                rLock.unlock();
            }
        }

    }
}
