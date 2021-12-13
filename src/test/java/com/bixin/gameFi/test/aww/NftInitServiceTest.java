package com.bixin.gameFi.test.aww;

import com.bixin.gameFi.GameFiApplication;
import com.bixin.gameFi.aww.service.AWWContractService;
import com.bixin.gameFi.aww.service.chain.ContractService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.math.BigInteger;

@ActiveProfiles("local")
@SpringBootTest(classes = GameFiApplication.class)
class NftInitServiceTest {
    @Resource
    private AWWContractService awwContractService;
    @Resource
    private ContractService contractService;

    @Test
    void initNFTMarket() {
        awwContractService.initNFTMarket(new BigInteger("0"), new BigInteger("50"));
    }

    @Test
    @SneakyThrows
    void createNFT() {
//        awwContractService.createNFT();
    }

    @Test
    @SneakyThrows
    void tempCreateNFT() {
        awwContractService.tempCreateNFT();
    }

    @Test
    void sellNFT() {
        assert awwContractService.sellNFT();
    }

    @Test
    void mintArm() {
        awwContractService.playerMintArm();
    }

}