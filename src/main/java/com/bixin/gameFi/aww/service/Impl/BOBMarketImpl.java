package com.bixin.gameFi.aww.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bixin.gameFi.aww.bean.dto.ChainResourcesDto;
import com.bixin.gameFi.aww.config.BOBConfig;
import com.bixin.gameFi.aww.service.IBOBMarketService;
import com.bixin.gameFi.common.utils.HexStringUtil;
import com.bixin.gameFi.common.utils.JacksonUtil;
import com.bixin.gameFi.core.contract.ContractBiz;
import com.bixin.gameFi.core.redis.RedisCache;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author renjian
 * @Date 2022/3/21 11:44
 */
@Slf4j
@Service
public class BOBMarketImpl implements IBOBMarketService {

    @Resource
    RedisCache redisCache;

    @Resource
    ContractBiz contractService;

    @Resource
    BOBConfig bobConfig;

    private static final String separator = "::";
    private static final String bobSuffix = separator + "BOBConfigV3" + separator + "Config";
    private static final String bobSuffix_ReceInfo = separator + "BOBNormalRaceV1" + separator + "RaceInfo";


    @Override
    public Map getBOBConfig() {
        JSONObject configInfo = new JSONObject();
        Map configInfoMap = (Map) pullBOBResource(bobSuffix);
        if (configInfoMap == null) {
            return configInfo;
        }

        Map<String, String> normal_ticket_fee_token = (Map<String, String>) configInfoMap.get("normal_ticket_fee_token");
        //将modulename和name由16进制转换为字符串
        String modulName = HexStringUtil.toStringHex(String.valueOf(normal_ticket_fee_token.get("module_name")).replaceAll("0x", ""));
        String name = HexStringUtil.toStringHex(String.valueOf(normal_ticket_fee_token.get("name")).replaceAll("0x", ""));

        normal_ticket_fee_token.put("module_name", modulName);
        normal_ticket_fee_token.put("name", name);
        //重新赋值total_reward_token
        configInfoMap.put("normal_ticket_fee_token", normal_ticket_fee_token);
        configInfo = new JSONObject(configInfoMap);
        return configInfo;
    }

    @Override
    public JSONObject getBOBRaceInfo() {
        JSONObject raceInfo = new JSONObject();
        Map receInfoMap = (Map) pullBOBResource(bobSuffix_ReceInfo);
        if (receInfoMap == null) {
            return raceInfo;
        }
        //去掉event数据
        receInfoMap.remove("sign_up_events");
        receInfoMap.remove("exit_events");
        receInfoMap.remove("eliminate_events");

        Map<String, String> total_reward_token = (Map<String, String>) receInfoMap.get("total_reward_token");
        //将modulename和name由16进制转换为字符串
        String modulName = HexStringUtil.toStringHex(String.valueOf(total_reward_token.get("module_name")).replaceAll("0x", ""));
        String name = HexStringUtil.toStringHex(String.valueOf(total_reward_token.get("name")).replaceAll("0x", ""));

        total_reward_token.put("module_name", modulName);
        total_reward_token.put("name", name);
        //重新赋值total_reward_token
        receInfoMap.put("total_reward_token", total_reward_token);

        List items = (List) receInfoMap.get("items");
        Integer state = (Integer) receInfoMap.get("state");
        if (state == 2) {

        }
        raceInfo = new JSONObject(receInfoMap);
        return raceInfo;
    }

    private Object pullBOBResource(String key) {
        String resource = contractService.listResource(bobConfig.getCommon().getContractAddress());
        ChainResourcesDto chainResourcesDto = JacksonUtil.readValue(resource, new TypeReference<>() {
        });
        if (Objects.isNull(chainResourcesDto) || Objects.isNull(chainResourcesDto.getResult())
                || Objects.isNull(chainResourcesDto.getResult().getResources())) {
            log.error("bob Market get chain resource is empty {}", chainResourcesDto);
            return null;
        }

        log.info("bobMarket get resource info: {}", resource);
        String bobKey = bobConfig.getCommon().getContractAddress() + key;
        //根据key获取到Resource信息
        Object value = chainResourcesDto.getResult().getResources().get(bobKey);

        Map<String, Object> map = (Map<String, Object>) value;
        if (CollectionUtils.isEmpty(map) || !map.containsKey("json")) {
            log.error("bob resource json is empty,key:" + key);
            return null;
        }
        return map.get("json");
    }
}
