package com.bixin.gameFi.aww.controller;

import com.bixin.gameFi.aww.bean.DO.AwwMarket;
import com.bixin.gameFi.aww.bean.DO.AwwMatchRecords;
import com.bixin.gameFi.aww.common.contants.PathConstant;
import com.bixin.gameFi.aww.common.response.P;
import com.bixin.gameFi.aww.service.IAwwMarketService;
import com.bixin.gameFi.aww.service.IAwwStoreService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhangcheng
 * create  2021/12/9
 */
@RestController
@RequestMapping(PathConstant.AWW_REQUEST_PATH_PREFIX + "/store")
public class AwwStoreController {

    @Resource
    IAwwStoreService awwStoreService;
    @Resource
    IAwwMarketService awwMarketService;

    @GetMapping("/getSelling")
    public P getSelling(@RequestParam(value = "address", defaultValue = "") String address,
                        @RequestParam(value = "pageSize", defaultValue = "20") long pageSize,
                        @RequestParam(value = "pageNum", defaultValue = "0") long pageNum) {

        if (pageNum < 0 || pageSize <= 0 || StringUtils.isEmpty(address)) {
            return P.failed("parameter is invalid");
        }
        List<AwwMarket> awwMarkets = awwMarketService.selectByPages(true, null, null, null, null,
                pageSize + 1, pageNum, 0);
        if (CollectionUtils.isEmpty(awwMarkets)) {
            return P.success(null, false);
        }

        boolean hasNext = false;
        if (awwMarkets.size() > pageSize) {
            awwMarkets = awwMarkets.subList(0, awwMarkets.size() - 1);
            hasNext = true;
        }

        return P.success(awwMarkets, hasNext);
    }

    @GetMapping("/getSellRecords")
    public P getSellRecords(@RequestParam(value = "address", defaultValue = "") String address,
                            @RequestParam(value = "pageSize", defaultValue = "20") long pageSize,
                            @RequestParam(value = "pageNum", defaultValue = "0") long pageNum) {

        if (pageNum < 0 || pageSize <= 0 || StringUtils.isEmpty(address)) {
            return P.failed("parameter is invalid");
        }
        List<AwwMatchRecords> matchRecords = awwStoreService.selectByPages(true, address, "", pageSize + 1, pageNum, 0);
        if (CollectionUtils.isEmpty(matchRecords)) {
            return P.success(null, false);
        }

        boolean hasNext = false;
        if (matchRecords.size() > pageSize) {
            matchRecords = matchRecords.subList(0, matchRecords.size() - 1);
            hasNext = true;
        }

        return P.success(matchRecords, hasNext);
    }


}
