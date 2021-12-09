package com.bixin.gameFi.aww.service.chain;

import com.bixin.gameFi.aww.config.AwwConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.starcoin.bean.ListResourceOption;
import org.starcoin.types.AccountAddress;
import org.starcoin.utils.AccountAddressUtils;
import org.starcoin.utils.StarcoinClient;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Component
public class ContractService {

    @Resource
    private AwwConfig awwConfig;

    private StarcoinClient starcoinClient;


    @PostConstruct
    public void init() {
        starcoinClient = new StarcoinClient(awwConfig.getCommon().getUrl(), awwConfig.getCommon().getChainId());
    }

    /**
     * 获取所有Resource
     *
     * @param senderAddress
     * @return
     */
    public String listResource(String senderAddress) {
        AccountAddress sender = AccountAddressUtils.create(senderAddress);
        ListResourceOption listResourceOption = new ListResourceOption();
        listResourceOption.setDecode(true);
        return starcoinClient.call("state.list_resource", Lists.newArrayList(new Object[]{AccountAddressUtils.hex(sender), listResourceOption}));
    }

}
