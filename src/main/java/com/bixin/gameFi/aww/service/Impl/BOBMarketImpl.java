package com.bixin.gameFi.aww.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bixin.gameFi.aww.bean.DO.BOBMintInfo;
import com.bixin.gameFi.aww.bean.dto.ChainResourcesDto;
import com.bixin.gameFi.aww.config.BOBConfig;
import com.bixin.gameFi.aww.core.mapper.BobMintInfoMapper;
import com.bixin.gameFi.aww.service.IBOBMarketService;
import com.bixin.gameFi.common.utils.HexStringUtil;
import com.bixin.gameFi.common.utils.JacksonUtil;
import com.bixin.gameFi.core.contract.ContractBiz;
import com.bixin.gameFi.core.redis.RedisCache;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.beans.Beans;
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

    @Resource
    BobMintInfoMapper bobMintInfoMapper;

    private static final String separator = "::";
    private static final String bobSuffix = separator + "BOBConfigV4" + separator + "Config";
    private static final String bobSuffix_NormalTicket = "BOBNormalTicketV3";
    private static final String bobSuffix_ReceInfo = separator + "BOBNormalRaceV2" + separator + "RaceInfo";

//0x00000000000000000000000000000001::NFTGallery::NFTGallery<0x9b996121ea29b50c6213558e34120e5c::BOBNormalTicketV3::Meta, 0x9b996121ea29b50c6213558e34120e5c::BOBNormalTicketV3::Body>
    @Override
    public JSONObject getBOBMintInfo(String account) {
        account = account.toLowerCase();
        BOBMintInfo bobMintInfo = bobMintInfoMapper.selectByState();
        if (bobMintInfo == null)  {
            return null;
        }
        //todo:判空并且加锁
        bobMintInfo.setState(1);
        bobMintInfo.setAccount(account);
        bobMintInfoMapper.updateByPrimaryKeySelective(bobMintInfo);
        return JSONObject.parseObject(JacksonUtil.toJson(bobMintInfo));
    }

    @Override
    public JSONArray getNormalTicket(String account) {
        account = account.toLowerCase();
        String key = "0x00000000000000000000000000000001::NFTGallery::NFTGallery<" + bobConfig.getCommon().getContractAddress() + separator + bobSuffix_NormalTicket + separator + "Meta, " + bobConfig.getCommon().getContractAddress() + separator + bobSuffix_NormalTicket + separator + "Body>";
        Map normalTicketMap = (Map) pullBOBResource(key, account);

//        List items = (List) normalTicketMap.get("items");
//        JSONArray itemArry = buildRaceInfoItems(items);

        JSONArray normalTickets = JSON.parseArray(JSONObject.toJSONString(normalTicketMap.get("items")));
        JSONArray result = new JSONArray();
        for (int i = 0; i < normalTickets.size();i++) {
            JSONObject item = normalTickets.getJSONObject(i);

            //解析base_meta
            JSONObject meta = item.getJSONObject("base_meta");
            String image = HexStringUtil.toStringHex(meta.getString("image").replaceAll("0x", ""));
            String name = HexStringUtil.toStringHex(meta.getString("name").replaceAll("0x", ""));
            String description = HexStringUtil.toStringHex(meta.getString("description").replaceAll("0x", ""));
            item.put("image", image);
            item.put("name", name);
            item.put("description", description);
            item.remove("base_meta");
            JSONObject type_meta = item.getJSONObject("type_meta");
            item.put("used",type_meta.getBoolean("used"));
            item.remove("base_meta");
            item.remove("type_meta");
            item.remove("body");
            result.add(item);
        }

        return result;
    }

    @Override
    public Map getBOBConfig() {
        //todo:需要放到redis中
        JSONObject configInfo = new JSONObject();
        Map configInfoMap = (Map) pullBOBResource(bobSuffix, null);
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
        Map raceInfoMap = (Map) pullBOBResource(bobSuffix_ReceInfo, null);
        if (raceInfoMap == null) {
            return raceInfo;
        }
        //添加合约数据
        raceInfoMap.put("raceContract", bobConfig.getCommon().getContractAddress() + bobSuffix_ReceInfo);
        raceInfoMap.put("normalTicketMeta",bobConfig.getCommon().getContractAddress() + separator + bobSuffix_NormalTicket);
        raceInfoMap.put("normalTicketMBody",bobConfig.getCommon().getContractAddress() + separator + bobSuffix_NormalTicket);


        //去掉event数据
        raceInfoMap.remove("sign_up_events");
        raceInfoMap.remove("exit_events");
        raceInfoMap.remove("eliminate_events");

        Map<String, String> total_reward_token = (Map<String, String>) raceInfoMap.get("total_reward_token");
        //将modulename和name由16进制转换为字符串
        String modulName = HexStringUtil.toStringHex(String.valueOf(total_reward_token.get("module_name")).replaceAll("0x", ""));
        String name = HexStringUtil.toStringHex(String.valueOf(total_reward_token.get("name")).replaceAll("0x", ""));

        total_reward_token.put("module_name", modulName);
        total_reward_token.put("name", name);
        //重新赋值total_reward_token
        raceInfoMap.put("total_reward_token", total_reward_token);

        List items = (List) raceInfoMap.get("items");
        Integer state = (Integer) raceInfoMap.get("state");
        if (state == 2) {//如果是竞赛开始状态，返回已报名的nft
            JSONArray itemArry = buildRaceInfoItems(items);
            raceInfoMap.put("items",itemArry);
        }else {
            raceInfoMap.remove("items");
        }
        raceInfo = new JSONObject(raceInfoMap);
        return raceInfo;
    }

    //调用stat.list_resource公共方法
    private Object pullBOBResource(String key, String account) {
        if (account == null) {
            account = bobConfig.getCommon().getContractAddress();
        }
        String resource = contractService.listResource(account);
        ChainResourcesDto chainResourcesDto = JacksonUtil.readValue(resource, new TypeReference<>() {
        });
        if (Objects.isNull(chainResourcesDto) || Objects.isNull(chainResourcesDto.getResult())
                || Objects.isNull(chainResourcesDto.getResult().getResources())) {
            log.error("bob Market get chain resource is empty {}", chainResourcesDto);
            return null;
        }

        log.info("bobMarket get resource info: {}", resource);
        //如果以::开头，则转换一下，加上用户address
        if (key.startsWith(separator)) {
            key = bobConfig.getCommon().getContractAddress() + key;
        }

        //根据key获取到Resource信息
        Object value = chainResourcesDto.getResult().getResources().get(key);

        Map<String, Object> map = (Map<String, Object>) value;
        if (CollectionUtils.isEmpty(map) || !map.containsKey("json")) {
            log.error("bob resource json is empty,key:" + key);
            return null;
        }
        return map.get("json");
    }

    /**
     * 构建items返回体
     * @param items
     * @return
     */
    private JSONArray buildRaceInfoItems(List items) {
        JSONArray itemArr = new JSONArray();
        items.forEach(item -> {
            JSONObject itemObj = JSONObject.parseObject(JSONObject.toJSONString(item));
            JSONObject nft = itemObj.getJSONObject("nft");

            JSONArray vec = nft.getJSONArray("vec");
            JSONObject vecItem = vec.getJSONObject(0);
            itemObj.put("creator",vecItem.getString("creator"));
            itemObj.put("id",vecItem.getString("id"));

            //解析base_meta信息
            JSONObject base_meta = vecItem.getJSONObject("base_meta");
            itemObj.put("name",HexStringUtil.toStringHex(base_meta.getString("name").replaceAll("0x", "")));
            itemObj.put("image",HexStringUtil.toStringHex(base_meta.getString("image").replaceAll("0x", "")));
            itemObj.put("description",HexStringUtil.toStringHex(base_meta.getString("description").replaceAll("0x", "")));

            JSONObject type_meta = vecItem.getJSONObject("type_meta");
            itemObj.put("used",type_meta.getBooleanValue("used"));

            //去掉原有的nft结构
            itemObj.remove("nft");

            itemArr.add(itemObj);

        });
        return itemArr;
    }
}
