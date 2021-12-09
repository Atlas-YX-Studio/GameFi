package com.bixin.gameFi.aww.controller;

import com.bixin.gameFi.aww.bean.DO.AwwMarket;
import com.bixin.gameFi.aww.common.contants.PathConstant;
import com.bixin.gameFi.aww.common.response.P;
import com.bixin.gameFi.aww.service.IAwwMarketService;
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
@RequestMapping(PathConstant.AWW_REQUEST_PATH_PREFIX + "/market")
public class AwwMarketController {

    @Resource
    IAwwMarketService awwMarketService;

    @GetMapping("/getALL")
    public P getALL(@RequestParam(value = "groupId", defaultValue = "0") long groupId,
                    @RequestParam(value = "startPrice", defaultValue = "0") long startPrice,
                    @RequestParam(value = "endPrice", defaultValue = "0") long endPrice,
                    @RequestParam(value = "sort", defaultValue = "0") int sort,
                    @RequestParam(value = "rarity", defaultValue = "all") String rarity,
                    @RequestParam(value = "pageSize", defaultValue = "20") long pageSize,
                    @RequestParam(value = "pageNum", defaultValue = "0") long pageNum) {

        if (groupId < 0 || pageNum < 0 || pageSize <= 0 || sort < 0
                || startPrice < 0 || endPrice <= 0 || endPrice < startPrice
                || StringUtils.isEmpty(rarity)) {
            return P.failed("parameter is invalid");
        }

        List<AwwMarket> awwMarkets = awwMarketService.selectByPages(true,
                groupId,
                startPrice,
                endPrice,
                rarity,
                pageSize + 1,
                pageNum,
                sort);

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

}
