package com.bixin.gameFi.aww.scheduler;

import com.bixin.gameFi.aww.bean.dto.AwwChainMarketDto;
import com.bixin.gameFi.aww.bean.dto.ChainResourceDto;
import com.bixin.gameFi.common.utils.JacksonUtil;
import com.bixin.gameFi.aww.config.AwwConfig;
import com.bixin.gameFi.core.redis.RedisCache;
import com.bixin.gameFi.aww.service.IAwwMarketService;
import com.bixin.gameFi.aww.service.IAwwStoreService;
import com.bixin.gameFi.core.contract.ContractService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author zhangcheng
 * create  2021/12/9
 */
@Slf4j
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


    private static final String separator = "::";
    private static final String awwSuffix = separator + "ARMMarket" + separator + "ARMSelling";


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
        String resource = contractService.listResource(awwConfig.getCommon().getContractAddress());
        ChainResourceDto chainResourceDto = JacksonUtil.readValue(resource, new TypeReference<>() {
        });
        if (Objects.isNull(chainResourceDto) || Objects.isNull(chainResourceDto.getResult())
                || Objects.isNull(chainResourceDto.getResult().getResources())) {
            log.warn("AwwMarketScheduler get chain resource is empty {}", chainResourceDto);
        }
        String awwKey = awwConfig.getCommon().getContractAddress() + awwSuffix;

        chainResourceDto.getResult().getResources().forEach((key, value) -> {
            try {
                if (!key.startsWith(awwKey)) {
                    return;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                if (CollectionUtils.isEmpty(map) || !map.containsKey("json")) {
                    return;
                }
                AwwChainMarketDto nftBoxDto = JacksonUtil.readValue(JacksonUtil.toJson(map.get("json")), new TypeReference<>() {
                });






            } catch (Exception e) {
                log.error("AwwMarketScheduler exception {}, {}", key, value, e);
            }
        });

    }


}
