package com.bixin.gameFi.aww.controller;

import com.bixin.gameFi.aww.common.contants.PathConstant;
import com.bixin.gameFi.aww.service.IBOBMarketService;
import com.bixin.gameFi.aww.service.Impl.BOBMarketImpl;
import com.bixin.gameFi.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author renjian
 * @Date 2022/3/21 11:27
 */
@RestController
@RequestMapping(PathConstant.BOB_REQUEST_PATH_PREFIX)
public class BOBMarketController {
    @Autowired
    IBOBMarketService bobMarketService;

    @GetMapping("text")
    public R getBOBResoruces() {
        R r = R.success(bobMarketService.getBOBConfig());
        return r;
    }

    @GetMapping("raceInfo")
    public R getBOBReceInfo() {
        R r = R.success(bobMarketService.getBOBRaceInfo());
        return r;
    }


}
