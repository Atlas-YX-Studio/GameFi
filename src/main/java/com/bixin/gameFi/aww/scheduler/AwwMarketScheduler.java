package com.bixin.gameFi.aww.scheduler;

import com.bixin.gameFi.aww.bean.DO.AwwArmInfo;
import com.bixin.gameFi.aww.bean.DO.AwwMarket;
import com.bixin.gameFi.aww.bean.dto.AwwChainMarketDto;
import com.bixin.gameFi.aww.bean.dto.ChainResourceDto;
import com.bixin.gameFi.aww.config.AwwConfig;
import com.bixin.gameFi.aww.service.IAwwArmInfoService;
import com.bixin.gameFi.aww.service.IAwwMarketService;
import com.bixin.gameFi.aww.service.IAwwStoreService;
import com.bixin.gameFi.common.utils.JacksonUtil;
import com.bixin.gameFi.core.contract.ContractBiz;
import com.bixin.gameFi.core.redis.RedisCache;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
    IAwwArmInfoService awwArmInfoService;
    @Resource
    ContractBiz contractService;


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

        List<AwwMarket> awwMarketList = new ArrayList<>();
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
                AwwChainMarketDto awwChainMarketDto = JacksonUtil.readValue(JacksonUtil.toJson(map.get("json")), new TypeReference<>() {
                });
                if (CollectionUtils.isEmpty(awwChainMarketDto.getItems())) {
                    return;
                }
                awwChainMarketDto.getItems().forEach(item -> awwMarketList.add(buildAwwMarket(item)));
            } catch (Exception e) {
                log.error("AwwMarketScheduler exception {}, {}", key, value, e);
            }
        });
        if(CollectionUtils.isEmpty(awwMarketList)){
            awwMarketService.deleteAll();
            return;
        }
        List<AwwMarket> oldMarkets = awwMarketService.selectAll();
//        List<AwwArmInfo> awwArmInfos = awwArmInfoService.selectAll();


        Map<Long, List<AwwMarket>> newMarketMap = awwMarketList.stream().collect(Collectors.groupingBy(AwwMarket::getChainId));
        Map<Long, List<AwwMarket>> oldMarketMap = oldMarkets.stream().collect(Collectors.groupingBy(AwwMarket::getChainId));
        Map<Long, List<AwwArmInfo>> armInfos = awwArmInfos.stream().collect(Collectors.groupingBy(AwwArmInfo::getArmId));

        List<Long> delIds = new ArrayList<>();
        List<AwwMarket> updates = new ArrayList<>();
        List<AwwMarket> inserts = new ArrayList<>();



    }


    private AwwMarket buildAwwMarket(AwwChainMarketDto.Item item) {
        AwwChainMarketDto.NFTVec vec = item.getNft().getVec().get(0);
        AwwChainMarketDto.TypeMeta typeMeta = vec.getType_meta();
        AwwChainMarketDto.BodyMeta bodyMeta = vec.getBody();
        AwwMarket.AwwMarketBuilder marketBuilder = AwwMarket.builder()
                .chainId(item.getId())
//                .awwId()
//                .awwName()
//                .icon()
                .owner(item.getSeller())
                .address(awwConfig.getCommon().getContractAddress())
                .rarity(typeMeta.getRarity())
                .sellPrice(item.getSelling_price())
                .stamina(typeMeta.getStamina())
                .usedStamina(bodyMeta.getUsed_stamina())
                .winRateBonus(typeMeta.getWin_rate_bonus());
        return marketBuilder.build();
    }


//    public class AwwMarket {
//        private Long id;
//
//        private Long chainId;
//
//        private Long awwId;
//
//        private String awwName;
//
//        private String owner;
//
//        private String address;
//
//        private Integer rarity;
//
//        private BigDecimal sellPrice;
//
//        private Integer stamina;
//
//        private Integer usedStamina;
//
//        private Integer winRateBonus;
//
//        private String icon;
//
//        private Long createTime;
//
//        private Long updateTime;
//
//
//    }


}
