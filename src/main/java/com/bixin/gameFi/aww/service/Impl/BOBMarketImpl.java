package com.bixin.gameFi.aww.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bixin.gameFi.aww.bean.DO.BOBMintInfo;
import com.bixin.gameFi.aww.bean.dto.ChainDto;
import com.bixin.gameFi.aww.bean.dto.ChainResourceDto;
import com.bixin.gameFi.aww.bean.dto.ChainResourcesDto;
import com.bixin.gameFi.aww.config.BOBConfig;
import com.bixin.gameFi.aww.core.mapper.BobMintInfoMapper;
import com.bixin.gameFi.aww.service.IBOBMarketService;
import com.bixin.gameFi.common.utils.HexStringUtil;
import com.bixin.gameFi.common.utils.JacksonUtil;
import com.bixin.gameFi.core.contract.ContractBiz;
import com.bixin.gameFi.core.redis.RedisCache;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
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
    private static final String bobSuffix = separator + "BOBConfigV1003" + separator + "Config";
    private static final String bobSuffix_NormalTicket = "BOBNormalTicketV1003";
    private static final String bobSuffix_NormalRace = "BOBNormalRaceV101010";
    private static final String bobSuffix_NormalRaceInfo = separator + bobSuffix_NormalRace + separator + "RaceInfo";
    private static String bobSuffix_Burn = "";

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
            int ticketState = type_meta.getInteger("state");
            item.put("state",ticketState);
            if (ticketState != 0) { //只查询未使用状态的，还可能包含冠军和已退回
                continue;
            }
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
        Map configInfoMap = (Map) pullBOBResource(bobConfig.getCommon().getContractAddress() + bobSuffix, null);
        if (configInfoMap == null) {
            return configInfo;
        }

//        Map<String, String> normal_ticket_fee_token = (Map<String, String>) configInfoMap.get("normal_ticket_fee_token");
//        //将modulename和name由16进制转换为字符串
//        String modulName = HexStringUtil.toStringHex(String.valueOf(normal_ticket_fee_token.get("module_name")).replaceAll("0x", ""));
//        String name = HexStringUtil.toStringHex(String.valueOf(normal_ticket_fee_token.get("name")).replaceAll("0x", ""));
//
//        normal_ticket_fee_token.put("module_name", modulName);
//        normal_ticket_fee_token.put("name", name);
//        //重新赋值total_reward_token
//        configInfoMap.put("normal_ticket_fee_token", normal_ticket_fee_token);
        configInfo = new JSONObject(configInfoMap);
        return configInfo;
    }

    /**
     * 查询竞赛信息
     * @param account
     * @return
     */
    @Override
    public JSONObject getBOBRaceInfo(String account) {
        //这个account是为了过滤当前用户已参赛的nft列表，所以pullresource的时候不需要传,使用manager的address
        if (!StringUtils.isEmpty(account)) {
           account = account.toLowerCase();//转换小写
        }

        JSONObject raceInfo = new JSONObject();
        Map raceInfoMap = (Map) pullBOBResource(bobConfig.getCommon().getContractAddress() + bobSuffix_NormalRaceInfo, null);
        if (raceInfoMap == null) {
            return raceInfo;
        }
        //格式化名称和图片
        String name = String.valueOf(raceInfoMap.get("name"));
        String image = String.valueOf(raceInfoMap.get("img"));
        if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(image)) {
            image = HexStringUtil.toStringHex(image.replaceAll("0x", ""));
            name = HexStringUtil.toStringHex(name.replaceAll("0x", ""));
            raceInfoMap.put("name", name);
            raceInfoMap.put("img", image);
        }


        //添加合约数据
        raceInfoMap.put("normalRaceContract", bobConfig.getCommon().getContractAddress() + separator + bobSuffix_NormalRace);
        raceInfoMap.put("normalTicketMeta",bobConfig.getCommon().getContractAddress() + separator + bobSuffix_NormalTicket);
        raceInfoMap.put("normalTicketMBody",bobConfig.getCommon().getContractAddress() + separator + bobSuffix_NormalTicket);


        //去掉event数据
        raceInfoMap.remove("sign_up_events");
        raceInfoMap.remove("exit_events");
        raceInfoMap.remove("eliminate_events");

        Map<String, String> total_reward_token = (Map<String, String>) raceInfoMap.get("total_reward_token");
        //将modulename和name由16进制转换为字符串
        String modulName = HexStringUtil.toStringHex(String.valueOf(total_reward_token.get("module_name")).replaceAll("0x", ""));
        String token_name = HexStringUtil.toStringHex(String.valueOf(total_reward_token.get("name")).replaceAll("0x", ""));

        total_reward_token.put("module_name", modulName);
        total_reward_token.put("name", token_name);
        //重新赋值total_reward_token
        raceInfoMap.put("total_reward_token", total_reward_token);

        List items = (List) raceInfoMap.get("items");
        Integer state = (Integer) raceInfoMap.get("state");
        raceInfoMap.put("actual_surplus_count", items.size());

        if (state == 1 || state == 2) {//如果是报名状态或者竞赛开始状态，返回当前用户的nft
            JSONArray itemArry = buildRaceInfoItems(items, account);
            raceInfoMap.put("items",itemArry);
        }else {
            raceInfoMap.remove("items");
        }

        //处理冠军信息
//        String championNFtId = String.valueOf(raceInfoMap.get("champion_nft_id"));
        String chappionNftImage = String.valueOf(raceInfoMap.get("champion_nft_img"));

//        championNFtId = HexStringUtil.toStringHex(championNFtId.replaceAll("0x", ""));
        chappionNftImage = HexStringUtil.toStringHex(chappionNftImage.replaceAll("0x", ""));
//        raceInfoMap.put("champion_nft_id", championNFtId);
        raceInfoMap.put("champion_nft_img", chappionNftImage);

        Long signUpStartBlock = Long.parseLong(String.valueOf(raceInfoMap.get("sign_up_start_block")));
        Long signUpEndBlock = Long.parseLong(String.valueOf(raceInfoMap.get("sign_up_end_block")));
        Long signUpStartInterval = -1L;
        Long signUpEndInterval = -1L;
        //查询当前区块
        Long currentBlock = getBlockNum();
        if (currentBlock < signUpStartBlock ) {//当前还没到报名开始时间
            signUpStartInterval = (signUpStartBlock - currentBlock)* 5000L;//按每块5s计算
        }

        if (currentBlock < signUpEndBlock ) {//当前还没到报名开始时间
            signUpEndInterval = (signUpEndBlock - currentBlock)* 5000L;
        }
        raceInfoMap.put("signUpStartInterval", signUpStartInterval);
        raceInfoMap.put("signUpEndInterval", signUpEndInterval);
        raceInfo = new JSONObject(raceInfoMap);
        return raceInfo;
    }

    @Override
    public JSONArray getBOBFallenInfo(String account) {
        if (!StringUtils.isEmpty(account)) {
            account = account.toLowerCase();
        }
        //查询burn地址
        if (StringUtils.isEmpty(bobSuffix_Burn)) {
            Map configMap = getBOBConfig();
            bobSuffix_Burn = String.valueOf(configMap.get("normal_burn_address"));
        }

        String burnKey = "0x00000000000000000000000000000001::NFTGallery::NFTGallery<" + bobConfig.getCommon().getContractAddress() + separator + bobSuffix_NormalTicket + separator + "Meta, " + bobConfig.getCommon().getContractAddress() + separator + bobSuffix_NormalTicket + separator + "Body>";
        Map resourceMap = (Map) pullBOBResource(burnKey, bobSuffix_Burn);
        JSONArray items = JSON.parseArray(JSONObject.toJSONString(resourceMap.get("items")));
        JSONArray result = new JSONArray();
        for (int i = 0; i < items.size();i++) {
            JSONObject item = items.getJSONObject(i);
            String creator = item.getString("creator");
            if (!StringUtils.isEmpty(account) && !account.equalsIgnoreCase(creator)) {
                continue;
            }

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
            item.put("state",type_meta.getInteger("state"));
            item.remove("base_meta");
            item.remove("type_meta");
            item.remove("body");
            result.add(item);
        }
        return result;
    }

    /**
     * 查询我的已报名列表，包含三部分，冠军、存活中的和被淘汰的
     * @param account
     * @return
     */
    @Override
    public JSONArray getMySignedNFT(String account) {
        //查询冠军
        //查询存活中的，需要放在前面
        JSONArray aliveArry = getMyRaceNFT(account);
        //查询被淘汰的,被淘汰的里面可能包含冠军，需要特殊处理
        JSONArray fallenArry = getBOBFallenInfo(account);
        log.debug("aliveSize:{},fallenSize:{}", aliveArry.size(), fallenArry.size());
        aliveArry.addAll(fallenArry);//合并
        return aliveArry;
    }

    //查询已参赛但是还没有被淘汰的NFT列表
    private JSONArray getMyRaceNFT(String account) {
        JSONArray aliveArry = new JSONArray();
        //这个account是为了过滤当前用户已参赛的nft列表，所以pullresource的时候不需要传
        if (!StringUtils.isEmpty(account)) {
            account = account.toLowerCase();//转换小写
        }

        Map raceInfoMap = (Map) pullBOBResource(bobConfig.getCommon().getContractAddress() + bobSuffix_NormalRaceInfo, null);
        if (raceInfoMap == null) {
            return aliveArry;
        }
        List items = (List) raceInfoMap.get("items");
        if (items == null || items.size() == 0) {
            return aliveArry;
        }
        aliveArry = buildRaceInfoItems(items, account);
        return aliveArry;
    }

    private JSONArray getNormalTicket1(String account) {
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
            int ticketState = type_meta.getInteger("state");
            item.put("state",ticketState);
            if (ticketState != 0) { //只查询未使用状态的，还可能包含冠军和已退回
                continue;
            }
            item.remove("base_meta");
            item.remove("type_meta");
            item.remove("body");
            result.add(item);
        }
        return result;
    }



    private Map pullResource(String currentAccount) {
//        String senderAddress = bobConfig.getCommon().getContractAddress();
        String key = "0x00000000000000000000000000000001::NFTGallery::NFTGallery<" + bobConfig.getCommon().getContractAddress() + separator + bobSuffix_NormalTicket + separator + "Meta, " + bobConfig.getCommon().getContractAddress() + separator + bobSuffix_NormalTicket + separator + "Body>";
        String resource = contractService.getResource(currentAccount, key);
        ChainResourceDto chainResourceDto = JacksonUtil.readValue(resource, new TypeReference<>() {
        });

        if (Objects.isNull(chainResourceDto) || Objects.isNull(chainResourceDto.getResult())
                || CollectionUtils.isEmpty(chainResourceDto.getResult().getJson())) {
            log.error("grantBuyBack get chain resource is empty {}", chainResourceDto);
            return null;
        }
        Map value = chainResourceDto.getResult().getJson();
        return value;
    }

    //调用stat.list_resource公共方法
    private Object pullBOBResourceList(String key, String account) {
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



    private Object pullBOBResource(String key, String account) {
        if (StringUtils.isEmpty(account)){
            account = bobConfig.getCommon().getContractAddress(); //默认查询manager中的数据,否则查询传入地址的数据
        }
        String resource = contractService.getResource(account, key);
        ChainResourceDto chainResourceDto = JacksonUtil.readValue(resource, new TypeReference<>() {
        });

        ChainResourceDto.ChainResource chainResourceDtoResult = chainResourceDto.getResult();

        Map <String, Object> resutJson = chainResourceDtoResult.getJson();;
        return resutJson;
    }

    /**
     * 构建items返回体
     * @param items
     * @return
     */
    private JSONArray buildRaceInfoItems(List items, String account) {
        JSONArray itemArr = new JSONArray();
        items.forEach(item -> {
            JSONObject itemObj = JSONObject.parseObject(JSONObject.toJSONString(item));
            String owner = itemObj.getString("owner").toLowerCase();
            if (!owner.equalsIgnoreCase(account)) {
                return;
            }
            JSONObject nft = itemObj.getJSONObject("nft");

            JSONArray vec = nft.getJSONArray("vec");
            if (vec == null || vec.isEmpty()) {//处理垃圾数据
                return;
            }
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

    private Long getBlockNum( ) {
        String ChainInfoString = contractService.getChainInfo();
        ChainDto chainDto = JacksonUtil.readValue(ChainInfoString, new TypeReference<>() {
        });

        Map<String, Object>  chainResult = chainDto.getResult();
        Map headMap = (Map) chainResult.get("head");
        Long number = Long.parseLong(String.valueOf(headMap.get("number")));
        return number;
    }
}
