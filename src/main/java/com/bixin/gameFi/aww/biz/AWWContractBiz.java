package com.bixin.gameFi.aww.biz;

import com.bixin.gameFi.aww.bean.DO.AwwArmInfo;
import com.bixin.gameFi.aww.bean.dto.ChainResourceDto;
import com.bixin.gameFi.aww.config.AwwConfig;
import com.bixin.gameFi.aww.core.mapper.AwwArmInfoMapper;
import com.bixin.gameFi.aww.core.wrapDDL.AwwArmInfoDDL;
import com.bixin.gameFi.common.code.GameErrCode;
import com.bixin.gameFi.common.exception.GameException;
import com.bixin.gameFi.common.utils.JacksonUtil;
import com.bixin.gameFi.core.contract.ContractBiz;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.novi.serde.Bytes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.starcoin.bean.ScriptFunctionObj;
import org.starcoin.utils.AccountAddressUtils;
import org.starcoin.utils.BcsSerializeHelper;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.*;

@Slf4j
@Component
public class AWWContractBiz {

    @Resource
    private AwwConfig awwConfig;
    @Resource
    private AwwArmInfoMapper awwArmInfoMapper;
    @Resource
    private ContractBiz contractService;

    private static final long DAY_FACTOR = 86400L;

    /**
     * 1.部署NFT Market
     * 2.部署NFT Scripts
     * 3.初始化config
     *
     * @return
     */
    public void initNFTMarket(BigInteger creatorFee, BigInteger platformFee) {
        if (!deployAWWContract()) {
            log.error("AWW 部署失败");
            throw new GameException(GameErrCode.CONTRACT_DEPLOY_FAILURE);
        }
        // 部署nft合约
        if (!deployArmContractWithImage()) {
            log.error("ARM合约 部署失败");
            throw new GameException(GameErrCode.CONTRACT_DEPLOY_FAILURE);
        }
        if (!contractService.deployContract(awwConfig.getCommon().getContractAddress(), "contract/aww/" + awwConfig.getContent().getMarketModule() + ".mv", null)) {
            log.error("AWW Market部署失败");
            throw new GameException(GameErrCode.CONTRACT_DEPLOY_FAILURE);
        }
        if (!contractService.deployContract(awwConfig.getCommon().getContractAddress(), "contract/aww/" + awwConfig.getContent().getAwwGameModule() + ".mv", null)) {
            log.error("AWW Game部署失败");
            throw new GameException(GameErrCode.CONTRACT_DEPLOY_FAILURE);
        }
        if (!contractService.deployContract(awwConfig.getCommon().getContractAddress(), "contract/aww/" + awwConfig.getContent().getGrantModule() + ".mv", null)) {
            log.error("Grant 部署失败");
            throw new GameException(GameErrCode.CONTRACT_DEPLOY_FAILURE);
        }
        if (!contractService.deployContract(awwConfig.getCommon().getContractAddress(), "contract/aww/" + awwConfig.getContent().getScriptsModule() + ".mv", null)) {
            log.error("AWW Scripts部署失败");
            throw new GameException(GameErrCode.CONTRACT_DEPLOY_FAILURE);
        }
        ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(awwConfig.getCommon().getContractAddress())
                .moduleName(awwConfig.getContent().getScriptsModule())
                .functionName("init_config")
                .args(Lists.newArrayList(
                        BcsSerializeHelper.serializeU128ToBytes(creatorFee),
                        BcsSerializeHelper.serializeU128ToBytes(platformFee)
                ))
                .build();
        if (!contractService.callFunction(awwConfig.getCommon().getContractAddress(), scriptFunctionObj)) {
            log.error("ARM Config初始化失败");
            throw new GameException(GameErrCode.CONTRACT_CALL_FAILURE);
        }

        scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(awwConfig.getCommon().getContractAddress())
                .moduleName(awwConfig.getContent().getScriptsModule())
                .functionName("init_game")
                .args(Lists.newArrayList(BcsSerializeHelper.serializeU64ToBytes(1641980453000L)))
                .build();
        if (!contractService.callFunction(awwConfig.getCommon().getContractAddress(), scriptFunctionObj)) {
            log.error("AWW GAME Config初始化失败");
            throw new GameException(GameErrCode.CONTRACT_CALL_FAILURE);
        }

        scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(awwConfig.getCommon().getContractAddress())
                .moduleName(awwConfig.getContent().getScriptsModule())
                .functionName("init_market")
                .args(Lists.newArrayList())
                .build();
        if (!contractService.callFunction(awwConfig.getCommon().getContractAddress(), scriptFunctionObj)) {
            log.error("market初始化失败");
            throw new GameException(GameErrCode.CONTRACT_CALL_FAILURE);
        }
    }

    /**
     * mint所有NFT
     *
     * @return
     */
    public void createArm() {
        // mint nft + 盲盒
        AwwArmInfoDDL selectAwwArmInfo = new AwwArmInfoDDL();
        selectAwwArmInfo.createCriteria().andCreatedEqualTo(Boolean.FALSE);
        // 取出该组下所有待铸造NFT
        List<AwwArmInfo> awwArmInfoDos = awwArmInfoMapper.selectByDDL(selectAwwArmInfo);
        if (CollectionUtils.isEmpty(awwArmInfoDos)) {
            return;
        }
        MutableInt armId = new MutableInt(1);
        // 获取该组最后一个id
        selectAwwArmInfo = new AwwArmInfoDDL();
        selectAwwArmInfo.createCriteria().andCreatedEqualTo(Boolean.TRUE);
        List<AwwArmInfo> createdAwwArmInfos = awwArmInfoMapper.selectByDDL(selectAwwArmInfo);
        if (!CollectionUtils.isEmpty(createdAwwArmInfos)) {
            createdAwwArmInfos.sort(Comparator.comparingLong(AwwArmInfo::getArmId).reversed());
            armId.setValue(createdAwwArmInfos.get(0).getArmId() + 1);
        }
        awwArmInfoDos.stream().sorted(Comparator.comparingLong(AwwArmInfo::getId)).forEach(awwArmInfoDo -> {
            awwArmInfoDo.setArmId(armId.longValue());
            awwArmInfoMapper.updateByPrimaryKeySelective(awwArmInfoDo);
            // 铸造NFT，存放图片url
            if (!mintArmNFTWithImage(awwArmInfoDo)) {
                log.error("ARM {} mint失败", awwArmInfoDo.getName());
                throw new GameException(GameErrCode.CONTRACT_CALL_FAILURE);
            }
            log.info("ARM {} mint成功", awwArmInfoDo.getName());
            awwArmInfoDo.setCreated(true);
            awwArmInfoDo.setUpdateTime(System.currentTimeMillis());
            awwArmInfoMapper.updateByPrimaryKeySelective(awwArmInfoDo);
            armId.add(1);
        });
    }

    /**
     * 部署AWW Token
     *
     * @return
     */
    private boolean deployAWWContract() {
        String moduleName = awwConfig.getContent().getAwwModule();
        String address = awwConfig.getCommon().getContractAddress();
        String path = "contract/aww/" + moduleName + ".mv";
        ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(address)
                .moduleName(moduleName)
                .functionName("init_aww")
                .tyArgs(Lists.newArrayList())
                .args(Lists.newArrayList())
                .build();
        return contractService.deployContract(address, path, scriptFunctionObj);
    }

    /**
     * 部署NFT Token，封面为图片url
     *
     * @return
     */
    private boolean deployArmContractWithImage() {
        String moduleName = awwConfig.getContent().getArmModule();
        String address = awwConfig.getCommon().getContractAddress();
        String path = "contract/aww/" + moduleName + ".mv";
        ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(address)
                .moduleName(moduleName)
                .functionName("init_with_image")
                .tyArgs(Lists.newArrayList())
                .args(Lists.newArrayList(
                        Bytes.valueOf(BcsSerializeHelper.serializeString(awwConfig.getContent().getNftName())),
                        Bytes.valueOf(BcsSerializeHelper.serializeString(awwConfig.getContent().getTitlePageImage())),
                        Bytes.valueOf(BcsSerializeHelper.serializeString(awwConfig.getContent().getDescription()))
                ))
                .build();
        return contractService.deployContract(address, path, scriptFunctionObj);
    }

    /**
     * mint NFT，存放图片url
     */
    private boolean mintArmNFTWithImage(AwwArmInfo awwArmInfoDo) {
        String address = awwConfig.getCommon().getContractAddress();

        ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(address)
                .moduleName(awwConfig.getContent().getArmModule())
                .functionName("mint_with_image")
                .args(Lists.newArrayList(
                        Bytes.valueOf(BcsSerializeHelper.serializeString(awwArmInfoDo.getName())),
                        Bytes.valueOf(BcsSerializeHelper.serializeString(awwConfig.getContent().getImageInfoApi() + awwArmInfoDo.getId())),
                        Bytes.valueOf(BcsSerializeHelper.serializeString(awwConfig.getContent().getDescription())),
                        Bytes.valueOf(BcsSerializeHelper.serializeU8(awwArmInfoDo.getRarity())),
                        Bytes.valueOf(BcsSerializeHelper.serializeU8(awwArmInfoDo.getStamina())),
                        Bytes.valueOf(BcsSerializeHelper.serializeU8(awwArmInfoDo.getWinRateBonus()))
                ))
                .build();
        return contractService.callFunction(address, scriptFunctionObj);
    }

    public void playerMintArm() {
        String moduleName = awwConfig.getContent().getScriptsModule();
        String address = awwConfig.getCommon().getContractAddress();
        ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(address)
                .moduleName(moduleName)
                .functionName("arm_mint")
                .tyArgs(Lists.newArrayList())
                .args(Lists.newArrayList())
                .build();
        if (!contractService.callFunction("0xC445808B4AecA9a5779908386a8673de", scriptFunctionObj)) {
            log.error("buy arm 失败");
            throw new GameException(GameErrCode.CONTRACT_CALL_FAILURE);
        }
    }

    public void fight() {
        String moduleName = awwConfig.getContent().getScriptsModule();
        String address = awwConfig.getCommon().getContractAddress();
        ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(address)
                .moduleName(moduleName)
                .functionName("fight")
                .tyArgs(Lists.newArrayList())
                .args(Lists.newArrayList(
                        BcsSerializeHelper.serializeU64ToBytes(5L),
                        Bytes.valueOf(BcsSerializeHelper.serializeU8((byte) 0)))
                )
                .build();
        if (!contractService.callFunction("0xC445808B4AecA9a5779908386a8673de", scriptFunctionObj)) {
            log.error("战斗 失败");
            throw new GameException(GameErrCode.CONTRACT_CALL_FAILURE);
        }
    }

    public boolean sellNFT() {
        String moduleName = awwConfig.getContent().getScriptsModule();
        String address = awwConfig.getCommon().getContractAddress();
        ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(address)
                .moduleName(moduleName)
                .functionName("arm_place_order")
                .args(Lists.newArrayList(
                        BcsSerializeHelper.serializeU64ToBytes(5L),
                        BcsSerializeHelper.serializeU128ToBytes(BigInteger.valueOf(200000000000L))
                ))
                .tyArgs(Lists.newArrayList())
                .build();
        return contractService.callFunction("0xC445808B4AecA9a5779908386a8673de", scriptFunctionObj);
    }

    public boolean mintAwwTo() {
        String moduleName = awwConfig.getContent().getScriptsModule();
//        String moduleName = "AWW";
        String address = awwConfig.getCommon().getContractAddress();

        ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(address)
                .moduleName(moduleName)
                .functionName("mint_aww_to")
                .args(Lists.newArrayList(
                        BcsSerializeHelper.serializeU128ToBytes(BigInteger.valueOf(600000_000000000L)),
                        BcsSerializeHelper.serializeAddressToBytes(AccountAddressUtils.create("0xdedc7865659fe0dab662da125bf40b32"))
                ))
                .tyArgs(Lists.newArrayList())
                .build();
        return contractService.callFunction(address, scriptFunctionObj);
    }

    public boolean setGrantAmount() {
        String address = awwConfig.getCommon().getContractAddress();
        String moduleName = awwConfig.getContent().getGrantModule();

        ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(address)
                .moduleName(moduleName)
                .functionName("set_amount")
                .args(Lists.newArrayList(
                        BcsSerializeHelper.serializeU128ToBytes(BigInteger.valueOf(10000_000000000L))
                ))
                .tyArgs(Lists.newArrayList())
                .build();
        return contractService.callFunction(address, scriptFunctionObj);
    }

    public boolean depositGrantAmount() {
        String address = awwConfig.getCommon().getContractAddress();
        String moduleName = awwConfig.getContent().getGrantModule();

        ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(address)
                .moduleName(moduleName)
                .functionName("deposit")
                .args(Lists.newArrayList(
                        BcsSerializeHelper.serializeU128ToBytes(BigInteger.valueOf(600000_000000000L))
                ))
                .tyArgs(Lists.newArrayList())
                .build();
        return contractService.callFunction(address, scriptFunctionObj);
    }

    public void grantBuyBack() {
        String address = awwConfig.getCommon().getContractAddress();
        String moduleName = awwConfig.getContent().getGrantModule();

        String resource = contractService.getResource(address, String.format("%s::%s::%s", address, moduleName, "Fund"));
        ChainResourceDto chainResourceDto = JacksonUtil.readValue(resource, new TypeReference<>() {
        });

        if (Objects.isNull(chainResourceDto) || Objects.isNull(chainResourceDto.getResult())
                || CollectionUtils.isEmpty(chainResourceDto.getResult().getJson())) {
            log.error("grantBuyBack get chain resource is empty {}", chainResourceDto);
            return;
        }
        if (!chainResourceDto.getResult().getJson().containsKey("last_buy_back_time")) {
            log.error("grantBuyBack resource last_buy_back_time is empty ");
            return;
        }

        long now = System.currentTimeMillis() / 1000;
        long lastBuyBackTime = Long.parseLong(chainResourceDto.getResult().getJson().get("last_buy_back_time").toString());
        if (now / DAY_FACTOR <= lastBuyBackTime / DAY_FACTOR) {
            log.info("grantBuyBack has been executed");
            return;
        }
        ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(address)
                .moduleName(moduleName)
                .functionName("buy_back")
                .args(Lists.newArrayList())
                .tyArgs(Lists.newArrayList())
                .build();
        contractService.callFunction(address, scriptFunctionObj);
    }

    public boolean autoAcceptToken() {
        ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress("0x00000000000000000000000000000001")
                .moduleName("AccountScripts")
                .functionName("enable_auto_accept_token")
                .args(Lists.newArrayList())
                .tyArgs(Lists.newArrayList())
                .build();
        return contractService.callFunction("0x89e1db66b5879bf19f5c1cc7d12d901a", scriptFunctionObj);
    }

}
