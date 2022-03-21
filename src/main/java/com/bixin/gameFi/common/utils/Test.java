package com.bixin.gameFi.common.utils;

import lombok.SneakyThrows;
import org.starcoin.api.BlockRPCClient;
import org.starcoin.bean.Block;
import org.starcoin.bean.RawTransaction;
import org.starcoin.bean.TypeObj;
import org.starcoin.bean.UserTransaction;
import org.starcoin.types.AccountAddress;
import org.starcoin.types.Ed25519PrivateKey;
import org.starcoin.types.TransactionPayload;
import org.starcoin.utils.*;

import java.math.BigInteger;
import java.net.URL;

/**
 * @Author renjian
 * @Date 2022/3/20 21:15
 */
public class Test {

    @SneakyThrows
    public static void main(String[] args) {
        StarcoinClient starcoinClient = new StarcoinClient(new ChainInfo("main", "https://main-seed.starcoin.org", 1));
        String address = "0x93BFd6328Ca4d77211c3103bF94275e6";
        String privateKeyString = "64eff386552c731b175b8e52e7a898014d8377d7ca5d5899e6a185fc0f26cfa5";
        Ed25519PrivateKey privateKey = SignatureUtils.strToPrivateKey(privateKeyString);
        AccountAddress sender = AccountAddressUtils.create(address);

        String toAddress = "0x6Ac685774A553637415f5538ce52D056";
        AccountAddress addressTo = AccountAddressUtils.create(toAddress);
        TypeObj typeObj = TypeObj.STC();

//        TransactionPayload payload = starcoinClient.buildTransferPayload(privateKey, typeObj, 1);
//        String rst = starcoinClient.submitTransaction(sender, privateKey, payload);

//        BigInteger.ONE
//        String rst = starcoinClient.transfer(sender, privateKey, addressTo, typeObj, BigInteger.ONE);

        Block blockByHeight;
//        URL url = new URL("https://main-seed.starcoin.org");
        BlockRPCClient blockRPCClient = new BlockRPCClient(new URL("https://main-seed.starcoin.org"));
        blockByHeight = blockRPCClient.getBlockByHeight(0);
//        blockByHeight.getTransactionList().get(0).getEvents();
//        blockByHeight.body.getUserTransactions().get(0).rawTransaction;
        RawTransaction a = blockByHeight.getBody().getUserTransactions().get(0).getRawTransaction();
        TransactionPayloadDeserializer d = new TransactionPayloadDeserializer();
//        d.deserialize(a.getPayload());
//        System.out.println(rst);
    }
}
