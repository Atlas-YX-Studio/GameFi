package com.bixin.gameFi.aww.scheduler;

import com.bixin.gameFi.aww.config.AwwConfig;
import com.bixin.gameFi.aww.core.redis.RedisCache;
import com.bixin.gameFi.aww.service.IAwwMarketService;
import com.bixin.gameFi.aww.service.IAwwStoreService;
import com.bixin.gameFi.aww.service.chain.ContractService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author zhangcheng
 * create  2021/12/9
 */
@Component
public class AwwMarketScheduler {

    @Resource
    AwwConfig awwConfig;
    @Resource
    RedisCache redisCache;
    @Resource
    IAwwMarketService awwMarketService;
    @Resource
    IAwwStoreService awwStoreService;
    @Resource
    ContractService contractService;


    private static final long PROCESSING_EXPIRE_TIME = 30 * 1000L;
    private static final long LOCK_EXPIRE_TIME = 0L;
    private static final String GET_NFT_MARKET_LOCK = "aww_nft_market_lock";


    //        @Scheduled(cron = "0/10 * * * * ?")
    @Scheduled(fixedDelay = 10000)
    public void awwNftMarketList() {
        redisCache.tryGetDistributedLock(
                GET_NFT_MARKET_LOCK,
                UUID.randomUUID().toString(),
                PROCESSING_EXPIRE_TIME,
                LOCK_EXPIRE_TIME,
                this::pullAwwNftMarketList);
    }

    private void pullAwwNftMarketList() {


    }

}
