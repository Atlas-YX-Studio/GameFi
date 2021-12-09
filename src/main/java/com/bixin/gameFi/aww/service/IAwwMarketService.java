package com.bixin.gameFi.aww.service;

import com.bixin.gameFi.aww.bean.DO.AwwMarket;

import java.util.List;

/**
 * @author zhangcheng
 * create  2021/12/9
 */
public interface IAwwMarketService {

    int insert(AwwMarket record);

    List<AwwMarket> selectByPages(boolean predicateNextPage, Long groupId, Long startPrice, Long endPrice, String rarity,
                                  Long pageSize, Long pageNum, int sort);

}
