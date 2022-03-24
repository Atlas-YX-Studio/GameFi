package com.bixin.gameFi.aww.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * @Author renjian
 * @Date 2022/3/21 11:37
 */
public interface IBOBMarketService {

    JSONObject getBOBMintInfo(String account);

    JSONArray getNormalTicket(String account);

    Map getBOBConfig();

    JSONObject getBOBRaceInfo(String account);

    JSONArray getBOBFallenInfo(String account);
}
