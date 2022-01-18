package com.bixin.gameFi.test.aww;

import com.bixin.gameFi.GameFiApplication;
import com.bixin.gameFi.aww.biz.AWWContractBiz;
import com.bixin.gameFi.core.contract.ContractBiz;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.math.BigInteger;

@ActiveProfiles("local")
@SpringBootTest(classes = GameFiApplication.class)
class NftInitServiceTest {
    @Resource
    private AWWContractBiz awwContractService;
    @Resource
    private ContractBiz contractService;

    @Test
    void initNFTMarket() {
        awwContractService.initNFTMarket(new BigInteger("0"), new BigInteger("80"));
    }

    @Test
    @SneakyThrows
    void createArm() {
        awwContractService.createArm();
    }

    @Test
    void sellNFT() {
        assert awwContractService.sellNFT();
    }

    @Test
    void mintArm() {
        awwContractService.playerMintArm();
    }

    @Test
    void mintAWW() {
        Assertions.assertTrue(awwContractService.mintAwwTo());
    }

    @Test
    void setGrantAmount() {
        Assertions.assertTrue(awwContractService.setGrantAmount());
    }

    @Test
    void depositGrantAmount() {
        Assertions.assertTrue(awwContractService.depositGrantAmount());
    }

    @Test
    void grantBuyBack() {
        awwContractService.grantBuyBack();
    }

    @Test
    void updateGameConfig() {
        Assertions.assertTrue(awwContractService.updateGameConfig());
    }

    @Test
    void autoAcceptToken() {
        awwContractService.autoAcceptToken();
    }



}