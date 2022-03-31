package com.bixin.gameFi.aww.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bixin.gameFi.aww.bean.DO.BOBMintInfo;
import com.bixin.gameFi.aww.bean.dto.ChainDto;
import com.bixin.gameFi.aww.bean.dto.ChainResourceDto;
import com.bixin.gameFi.aww.bean.dto.ChainResourcesDto;
import com.bixin.gameFi.aww.common.contants.PathConstant;
import com.bixin.gameFi.aww.config.BOBConfig;
import com.bixin.gameFi.aww.core.mapper.BobMintInfoMapper;
import com.bixin.gameFi.aww.service.IBOBMarketService;
import com.bixin.gameFi.common.utils.HexStringUtil;
import com.bixin.gameFi.common.utils.JacksonUtil;
import com.bixin.gameFi.core.contract.ContractBiz;
import com.bixin.gameFi.core.redis.RedisCache;
import com.fasterxml.jackson.core.type.TypeReference;
import jnr.ffi.annotations.In;
import kotlin.jvm.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

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

    private static JSONObject mintTicketFee = new JSONObject();

    private static String NFTImageURL = PathConstant.AWW_REQUEST_PATH_PREFIX + "/normal/getImage/";

    private static final String separator = "::";
//    private static final String bobSuffix = separator + "BOBConfigV1043" + separator + "Config";
//    private static final String bobSuffix_NormalTicket = "BOBNormalTicketV1043";
//    private static final String bobSuffix_SeniorTicket = "BOBSeniorTicketV1043";
//    private static final String bobSuffix_NormalRace = "BOBSeniorRaceV1043";
//    private static final String bobSuffix_NormalRaceInfo = separator + bobSuffix_NormalRace + separator + "RaceInfo";
    private static String bobSuffix_Burn = "";

//0x00000000000000000000000000000001::NFTGallery::NFTGallery<0x9b996121ea29b50c6213558e34120e5c::BOBNormalTicketV3::Meta, 0x9b996121ea29b50c6213558e34120e5c::BOBNormalTicketV3::Body>
    @Override
    @Transactional
    public JSONObject getBOBMintInfo(String account) {
        if (!checkAccount(account)) {
            JSONObject error = new JSONObject();
            error.put("errorAccount", 1);
            return error;
        }
        synchronized (this) {
            account = account.toLowerCase();
            BOBMintInfo bobMintInfo = bobMintInfoMapper.selectByState();
            if (bobMintInfo == null)  {
                return null;
            }

            bobMintInfo.setState(1);
            bobMintInfo.setAccount(account);
            bobMintInfo.setName(String.valueOf(bobMintInfo.getId()));
            bobMintInfo.setDescription(String.valueOf(bobMintInfo.getId()));
            bobMintInfoMapper.updateByPrimaryKeySelective(bobMintInfo);
            bobMintInfo.setImageLink(bobConfig.getContent().getImageInfoApi() + bobMintInfo.getId());
            return JSONObject.parseObject(JacksonUtil.toJson(bobMintInfo));
        }
    }

    private boolean checkAccount(String account) {
        if (!account.startsWith("0x")) {
            return false;
        }
        return true;
    }

    @Override
    public String getNFTImage(Long id) {
        BOBMintInfo bobMintInfo = bobMintInfoMapper.selectByPrimaryKey(id);
        if (bobMintInfo == null) {
            return "";
        }
        return bobMintInfo.getImageLink();
    }

    @Override
    public JSONObject getMintFee() {
        JSONObject fee = new JSONObject();
        Map configInfoMap = (Map) pullBOBResource(bobConfig.getCommon().getContractAddress() + bobConfig.getContent().getBobSuffix(), null);
        if (configInfoMap == null) {
            return fee;
        }

        if (!mintTicketFee.isEmpty()) {//放到缓存中，避免多次查询
            return mintTicketFee;
        }

        Long ticketFee = 0L;
        JSONObject tickentToken = new JSONObject();
        //当前是高级场，取高级场的费用配置
        if (bobConfig.getContent().getRaceModule().contains("Senior")) {
            Integer feeInt = (Integer) ((List)configInfoMap.get("senior_ticket_fee")).get(0);
            ticketFee = feeInt.longValue();;
            List tokenList = (List) configInfoMap.get("senior_ticket_fee_token");tickentToken = JSONObject.parseObject(JSON.toJSONString(tokenList.get(0)));
            tickentToken.put("name", HexStringUtil.toStringHex(tickentToken.getString("name").replaceAll("0x", "")));
            tickentToken.put("module_name", HexStringUtil.toStringHex(tickentToken.getString("module_name").replaceAll("0x", "")));
        }else {
            Integer feeInt = (Integer) ((List)configInfoMap.get("normal_ticket_fee")).get(0);
            ticketFee = feeInt.longValue();;
            List tokenList = (List) configInfoMap.get("normal_ticket_fee_token");
            tickentToken = JSONObject.parseObject(JSON.toJSONString(tokenList.get(0)));
            tickentToken.put("name", HexStringUtil.toStringHex(tickentToken.getString("name").replaceAll("0x", "")));
            tickentToken.put("module_name", HexStringUtil.toStringHex(tickentToken.getString("module_name").replaceAll("0x", "")));
        }
        fee.put("fee", ticketFee);
        fee.put("token", tickentToken);
        mintTicketFee = fee;
        return fee;
    }

    @Override
    public JSONArray getNormalTicket(String account, String raceType) {

        if (!(raceType.equalsIgnoreCase("normal") && bobConfig.getContent().getRaceModule().contains("Normal") || raceType.equalsIgnoreCase("senior") && bobConfig.getContent().getRaceModule().contains("Senior")) ) {
            return new JSONArray();
        }

        account = account.toLowerCase();

        Map normalTicketMap = getMyTickets(account,0, 0);
        JSONArray result = (JSONArray) normalTicketMap.get(0);

        return result;
    }

    @Override
    public Map getBOBConfig() {
        //todo:需要放到redis中
        JSONObject configInfo = new JSONObject();
        Map configInfoMap = (Map) pullBOBResource(bobConfig.getCommon().getContractAddress() + bobConfig.getContent().getBobSuffix(), null);
        if (configInfoMap == null) {
            return configInfo;
        }

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
        log.info("getBOBRaceInfo start.account:{}" + account);
        //这个account是为了过滤当前用户已参赛的nft列表，所以pullresource的时候不需要传,使用manager的address
        if (!StringUtils.isEmpty(account)) {
           account = account.toLowerCase();//转换小写
        }

        JSONObject raceInfo = new JSONObject();
        String raceKey = bobConfig.getCommon().getContractAddress() + separator + bobConfig.getContent().getRaceModule() + separator + "RaceInfo";
        Map raceInfoMap = (Map) pullBOBResource(raceKey, null);
        if (raceInfoMap == null) {
            return raceInfo;
        }

        String raceType = "normal";
        //区分是普通场还是高级场
        if (bobConfig.getContent().getRaceModule().contains("Senior")) {
            raceType = "senior";
        }
        raceInfoMap.put("raceType", raceType);

        log.info("receType:{}", raceType);

        //格式化名称和图片
        String name = String.valueOf(raceInfoMap.get("name"));
        String image = String.valueOf(raceInfoMap.get("img"));
        if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(image)) {
            image = HexStringUtil.toStringHex(image.replaceAll("0x", ""));
            name = HexStringUtil.toStringHex(name.replaceAll("0x", ""));
            raceInfoMap.put("name", name);
            raceInfoMap.put("img", image);
        }


        String ticket = bobConfig.getContent().getBobSuffixNormalTicket();
        if (raceType.equalsIgnoreCase("senior")) { // 高级场
            ticket = bobConfig.getContent().getBobSuffixSeniorTicket();
        }
        //添加合约数据
        raceInfoMap.put("normalRaceContract", bobConfig.getCommon().getContractAddress() + separator + bobConfig.getContent().getRaceModule());
        raceInfoMap.put("normalTicketMeta",bobConfig.getCommon().getContractAddress() + separator + ticket);
        raceInfoMap.put("normalTicketMBody",bobConfig.getCommon().getContractAddress() + separator + ticket);


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

        //处理剩余奖池信息
        Map surplueRewardMap = (Map) raceInfoMap.get("surplus_reward");
        raceInfoMap.put("surplus_reward", surplueRewardMap.get("value"));

        List items = (List) raceInfoMap.get("items");
        Integer state = (Integer) raceInfoMap.get("state");
        raceInfoMap.put("actual_surplus_count", items.size());
        log.info("state:{}", state);
        if (state == 1 || state == 2) {//如果是报名状态或者竞赛开始状态，返回当前用户的nft
            JSONArray itemArry = buildRaceInfoItems(items, account);
            raceInfoMap.put("items",itemArry);
        }else {
            raceInfoMap.remove("items");
        }

        //处理冠军信息
        if(state == 3) {
            String championAddress = String.valueOf(raceInfoMap.get("champion_address"));
            if (championAddress.equalsIgnoreCase("0x00000000000000000000000000000000")) { //冠军地址是否被改了，不是默认值了
                raceInfoMap.put("champion_address", "");
            }else {
                championAddress = desAddress(championAddress);//构造脱敏地址
                raceInfoMap.put("champion_address", championAddress);
            }
        }
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

        if (currentBlock < signUpEndBlock ) {//当前还没到报名结束时间
            signUpEndInterval = (signUpEndBlock - currentBlock)* 5000L;
        }
        raceInfoMap.put("signUpStartInterval", signUpStartInterval);
        raceInfoMap.put("signUpEndInterval", signUpEndInterval);
        raceInfoMap.put("currentBlock", currentBlock);
        raceInfo = new JSONObject(raceInfoMap);
        return raceInfo;
    }

    @Override
    public JSONArray getBOBFallenInfo(String account) {
        log.info("getBOBFallenInfo, account:{}", account);
        //如果传了用户，查询当前账户被淘汰的，如果没传，查询所有被淘汰的
        if (!StringUtils.isEmpty(account)) {
            account = account.toLowerCase();
        }
        //查询burn地址
        if (StringUtils.isEmpty(bobSuffix_Burn)) {
            Map configMap = getBOBConfig();
            bobSuffix_Burn = String.valueOf(configMap.get("normal_burn_address"));
            if (bobConfig.getContent().getRaceModule().contains("Senior")) {
                bobSuffix_Burn = String.valueOf(configMap.get("senior_burn_address"));
            }
        }
        log.info("burn address:{}" + bobSuffix_Burn );

        //判断竞赛类型普通or高级
        String ticket = bobConfig.getContent().getBobSuffixNormalTicket();
        if (bobConfig.getContent().getRaceModule().contains("Senior")) {
            ticket = bobConfig.getContent().getBobSuffixSeniorTicket();
        }

        //查询当前竞赛信息，获取竞赛times标志，为了判断是当前场次比赛被淘汰的
        String raceKey = bobConfig.getCommon().getContractAddress() + separator + bobConfig.getContent().getRaceModule() + separator + "RaceInfo";
        Map raceInfoMap = (Map) pullBOBResource(raceKey, null);
        String raceUseTimes = String.valueOf(raceInfoMap.get("usage_times"));

        String burnKey = "0x00000000000000000000000000000001::NFTGallery::NFTGallery<" + bobConfig.getCommon().getContractAddress() + separator + ticket + separator + "Meta, " + bobConfig.getCommon().getContractAddress() + separator + ticket + separator + "Body>";
        Map resourceMap = (Map) pullBOBResource(burnKey, bobSuffix_Burn);
        JSONArray items = JSON.parseArray(JSONObject.toJSONString(resourceMap.get("items")));
        JSONArray result = new JSONArray();
        for (int i = 0; i < items.size();i++) {
            JSONObject item = items.getJSONObject(i);
            String creator = item.getString("creator");
            if (!StringUtils.isEmpty(account) && !account.equalsIgnoreCase(creator)) {
                continue;
            }

            //填写脱敏后的所属地址
            creator = desAddress(creator);
            item.put("creator", creator);

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
            //根据usageTime 判断是否是当前场次的淘汰数据
            String usageTime = type_meta.getString("usage_times");
            if (StringUtils.isEmpty(usageTime) || !usageTime.equalsIgnoreCase(raceUseTimes)) {
                continue;
            }

            item.remove("base_meta");
            item.remove("type_meta");
            item.remove("body");
            result.add(item);
        }
        return result;
    }



    private JSONObject getHistoryChampion(String account) {
        //先去获取config中冠军的address列表
        Map configMap = getBOBConfig();
        List championList = (List) configMap.get("normal_champion");
        if (championList.size() <= 0) {
            return new JSONObject();
        }

        //获取历史场次的冠军
        String historyRace = bobConfig.getContent().getHistoryRace();
        List<JSONObject> historyRaceChampion = new ArrayList();
        if (!StringUtils.isEmpty(historyRace)) {
            String[] historyRaces = historyRace.split(",");
            for (int i = 0; i < historyRaces.length; i++) {
                //查询竞赛信息，查询竞赛冠军
                String raceKey = bobConfig.getCommon().getContractAddress() + separator + historyRaces[i] + separator + "RaceInfo";
                Map raceInfoMap = (Map) pullBOBResource(raceKey, null);

                String image = HexStringUtil.toStringHex(String.valueOf(raceInfoMap.get("champion_nft_img")).replaceAll("0x", ""));
                String nftId = String.valueOf(raceInfoMap.get("champion_nft_id"));
                String address = String.valueOf(raceInfoMap.get("champion_address"));

                JSONObject championObj = new JSONObject();
                championObj.put("image", image);
                championObj.put("nftId", nftId);
                championObj.put("nft", nftId);
                championObj.put("address", address);
                historyRaceChampion.add(championObj);
            }
        }

        //用链上的冠军列表跟历史竞赛的冠军匹配，匹配上就返回
        JSONObject result = new JSONObject();
        for (int i = 0; i < championList.size(); i++) {
            String championId = String.valueOf(championList.get(i));
            for (int j = 0; j < historyRaceChampion.size(); j++) {
                JSONObject championObj = historyRaceChampion.get(j);
                if (account.equalsIgnoreCase(championObj.getString("address")) &&  championId.equalsIgnoreCase(championObj.getString("address"))) {
                    String address = championObj.getString("address");
                    championObj.put("address", desAddress(address));//脱敏地址
                    result = championObj;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 查询我的已报名列表，包含冠军、存活中的
     * @param account
     * @return
     */
    @Override
    public JSONObject getMySignedNFT(String account) {
        JSONObject result = new JSONObject();

        JSONObject champion = getHistoryChampion(account);
        if (!champion.isEmpty()) {
            champion.put("seniorRaceModule", bobConfig.getCommon().getContractAddress() + separator + bobConfig.getContent().getSeniorRaceModule()); //高级场合约地址
            result.put("champion", champion);
        }
        //查询冠军、退赛的，到个人账户下查询
        int[] states = {0,2,4};//2:已退赛，4:冠军
        Map endAndChampionMap = getMyTickets(account, 1, states);
        JSONArray endArry = new JSONArray();
        if (endAndChampionMap.containsKey(2)) {
            endArry = (JSONArray) endAndChampionMap.get(2);
        }
        JSONArray championArry = new JSONArray();
        if (endAndChampionMap.containsKey(4)) {
            championArry = (JSONArray) endAndChampionMap.get(4);
        }
        //未报名的
        JSONArray ticketArry = new JSONArray();
        if (endAndChampionMap.containsKey(0)) {
            ticketArry = (JSONArray) endAndChampionMap.get(0);
        }


        //查询存活中的，需要放在前面,从race中查
        JSONArray aliveArry = getMyRaceNFT(account);

        log.debug("aliveSize:{},ticketSize:{}", aliveArry.size(), ticketArry.size());



        aliveArry.addAll(championArry);
        result.put("signArry", aliveArry); //已报名的包含存活中的，冠军，包含普通场和高级场
        result.put("endArry", endArry);
        result.put("ticketArry", ticketArry);
        return result;
    }

    //查询已参赛但是还没有被淘汰的NFT列表，race只能是普通或者高级，不可能同时存在，所以根据race配置的哪个，就查哪个里面的数据就行了
    private JSONArray getMyRaceNFT(String account) {
        JSONArray aliveArry = new JSONArray();
        //这个account是为了过滤当前用户已参赛的nft列表，所以pullresource的时候不需要传
        if (!StringUtils.isEmpty(account)) {
            account = account.toLowerCase();//转换小写
        }

        String raceKey = bobConfig.getCommon().getContractAddress() + separator + bobConfig.getContent().getRaceModule() + separator + "RaceInfo";
        Map raceInfoMap = (Map) pullBOBResource(raceKey, null);
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

    /**
     * 查询个人账户下nft数据
     * @param account
     * @param targetState
     * @param getType 查询类型  0：只查当前场次，普通或者高级，1:查询所有场次，包含普通和高级的
     * @return
     */
    private Map<Integer, JSONArray> getMyTickets(String account, int getType, int... targetState) {
        Map result = new HashMap();

        //判断竞赛类型普通or高级
        String ticket = bobConfig.getContent().getBobSuffixNormalTicket();
        if (bobConfig.getContent().getRaceModule().contains("Senior")) {
            ticket = bobConfig.getContent().getBobSuffixSeniorTicket();
        }

        JSONArray personTickets = new JSONArray();
        //个人账户下包含未使用的，已退回的，冠军三种状态的数据,
        account = account.toLowerCase();
        String key = "0x00000000000000000000000000000001::NFTGallery::NFTGallery<" + bobConfig.getCommon().getContractAddress() + separator + ticket + separator + "Meta, " + bobConfig.getCommon().getContractAddress() + separator + ticket + separator + "Body>";
        Map firstTicketMap = (Map) pullBOBResource(key, account);

        if (!firstTicketMap.isEmpty()) {
            personTickets = JSON.parseArray(JSONObject.toJSONString(firstTicketMap.get("items")));
        }

        if (getType == 1) {//取另一种竞赛数据
            ticket = ticket.equals(bobConfig.getContent().getBobSuffixNormalTicket()) ? bobConfig.getContent().getBobSuffixSeniorTicket() : bobConfig.getContent().getBobSuffixNormalTicket();
            //个人账户下包含未使用的，已退回的，冠军三种状态的数据,
            key = "0x00000000000000000000000000000001::NFTGallery::NFTGallery<" + bobConfig.getCommon().getContractAddress() + separator + ticket + separator + "Meta, " + bobConfig.getCommon().getContractAddress() + separator + ticket + separator + "Body>";
            Map secondTicketMap = (Map) pullBOBResource(key, account);

            if (!secondTicketMap.isEmpty()) {
                personTickets.addAll(JSON.parseArray(JSONObject.toJSONString(secondTicketMap.get("items"))));
            }

        }
        if (personTickets.size() == 0) {
            return result;
        }

        for (int i = 0; i < personTickets.size();i++) {
            JSONObject item = personTickets.getJSONObject(i);

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
            boolean match = Arrays.stream(targetState).anyMatch(e -> e == ticketState);
            if (!match) {
                continue;
            }
            item.remove("base_meta");
            item.remove("type_meta");
            item.remove("body");
            JSONArray itemArry = new JSONArray();
            if (result.containsKey(ticketState)) {
                itemArry = (JSONArray) result.get(ticketState);
                itemArry.add(item);
            }else {
                itemArry.add(item);
            }

            result.put(ticketState, itemArry);

        }
        return result;
    }

    public JSONArray getOtherNFT1(String account) {
        String key = bobConfig.getContent().getNftName();
        Map NFTInfoMap = (Map) pullBOBResource(key, account);
        if (NFTInfoMap.isEmpty()) {
            return new JSONArray();
        }

        JSONArray nftArry = JSON.parseArray(JSONObject.toJSONString(NFTInfoMap.get("items")));
        for (int i = 0; i< nftArry.size(); i++) {
            JSONObject nftItem = nftArry.getJSONObject(i);
            //todo：
            nftItem.put("NFTMeta", "0x7D6409B9974e68f969c92554422cA19b::ARM2::ARMMeta");
            nftItem.put("NFTBody", "0x7D6409B9974e68f969c92554422cA19b::ARM2::ARMMeta");
            nftItem.put("payToken", "0x1::STC::STC");
            //解析base_meta信息
            JSONObject base_meta = nftItem.getJSONObject("base_meta");
            nftItem.put("name",HexStringUtil.toStringHex(base_meta.getString("name").replaceAll("0x", "")));
            nftItem.put("image",HexStringUtil.toStringHex(base_meta.getString("image").replaceAll("0x", "")));
            nftItem.put("description",HexStringUtil.toStringHex(base_meta.getString("description").replaceAll("0x", "")));
            nftItem.remove("base_meta");
        }
        return nftArry;
    }

    public JSONArray getOtherNFT(String account) throws Exception {
        String nftConfig = bobConfig.getContent().getNftName();
        String[] nftArr = nftConfig.split("/");
        if (nftArr == null || nftArr.length < 1) {
            return new JSONArray();
        }
        String bobSuffix = bobConfig.getContent().getBobSuffix();
        String SeniorAWWTicket = bobSuffix.substring(0, bobSuffix.length() - 6) + "SeniorAWWTicket";

        Map<String, Long> rateMap = new HashMap();
        JSONArray result = new JSONArray();
        //遍历每个类型的nft，查询当前账户下的数据
        for (int i= 0; i < nftArr.length; i++) {
            String[] nftItem = nftArr[i].split(",");
            String NFTMeta = nftItem[0];
            String NFTBody = nftItem[1];
            String playToken = nftItem[2];
            String otherNFTKey = bobConfig.getCommon().getContractAddress() + SeniorAWWTicket + "<" + NFTMeta + " ," + NFTBody + " ," + playToken + ">";
            //查询体力值兑换STC的比率
            Long feeRate = 0L;
            if (rateMap.isEmpty() || !rateMap.containsKey(otherNFTKey)) {
                Map configInfoMap = (Map) pullBOBResource(otherNFTKey, null);
                if (configInfoMap.isEmpty() || !configInfoMap.containsKey("fee_rate")) {
                    throw new Exception();
                }
                feeRate = ((Integer) configInfoMap.get("fee_rate")).longValue();
                rateMap.put(otherNFTKey, feeRate);
            }else {
                feeRate = rateMap.get("otherNFTKey");
            }
            result.addAll(getOtherNFTList(NFTMeta, NFTBody, playToken, account, feeRate));
        }
        return result;
    }

    private JSONArray getOtherNFTList(String meta, String body, String playToken, String account, Long feeRate) {
        String key = "0x00000000000000000000000000000001::NFTGallery::NFTGallery<" + meta + ", " + body + ">";
        Map NFTInfoMap = (Map) pullBOBResource(key, account);
        if (NFTInfoMap.isEmpty()) {
            return new JSONArray();
        }

        JSONArray nftArry = JSON.parseArray(JSONObject.toJSONString(NFTInfoMap.get("items")));
        for (int i = 0; i< nftArry.size(); i++) {
            JSONObject nftItem = nftArry.getJSONObject(i);

            nftItem.put("NFTMeta", meta);
            nftItem.put("NFTBody", body);
            nftItem.put("payToken", playToken);
            //解析base_meta信息
            JSONObject base_meta = nftItem.getJSONObject("base_meta");
            nftItem.put("name",HexStringUtil.toStringHex(base_meta.getString("name").replaceAll("0x", "")));
            nftItem.put("image",HexStringUtil.toStringHex(base_meta.getString("image").replaceAll("0x", "")));
            nftItem.put("description",HexStringUtil.toStringHex(base_meta.getString("description").replaceAll("0x", "")));
            nftItem.remove("base_meta");

            //解析type_meta信息
            JSONObject type_meta = nftItem.getJSONObject("type_meta");
            nftItem.put("stamina", type_meta.getInteger("stamina"));
            nftItem.put("feeRate", feeRate);
        }
        return nftArry;
    }

    private String desAddress(String address) {
        try {
            String startAdd = address.substring(0, 4);
            String endAdd = address.substring(address.length() - 3, address.length());
            address = startAdd + "..." + endAdd;
        }catch (Exception e) {
            log.error("address is not format, address:{}" + address);
            return "";
        }

        return address;
    }

    private Map pullResource(String currentAccount) {
//        String senderAddress = bobConfig.getCommon().getContractAddress();
        String key = "0x00000000000000000000000000000001::NFTGallery::NFTGallery<" + bobConfig.getCommon().getContractAddress() + separator + bobConfig.getContent().getBobSuffixNormalTicket() + separator + "Meta, " + bobConfig.getCommon().getContractAddress() + separator + bobConfig.getContent().getBobSuffixNormalTicket() + separator + "Body>";
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
        log.info("pullBOBRasource, key:{}, account:{}", key, account);
        if (StringUtils.isEmpty(account)){
            account = bobConfig.getCommon().getContractAddress(); //默认查询manager中的数据,否则查询传入地址的数据
        }
        String resource = contractService.getResource(account, key);
        ChainResourceDto chainResourceDto = JacksonUtil.readValue(resource, new TypeReference<>() {
        });

        ChainResourceDto.ChainResource chainResourceDtoResult = chainResourceDto.getResult();

        Map <String, Object> resutJson = new HashMap<>();
        if (chainResourceDtoResult != null) {
            resutJson = chainResourceDtoResult.getJson();
        }

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
            itemObj.put("state",type_meta.getInteger("state"));

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
