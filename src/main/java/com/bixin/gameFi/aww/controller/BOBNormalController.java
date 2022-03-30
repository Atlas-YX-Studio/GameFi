package com.bixin.gameFi.aww.controller;

import com.bixin.gameFi.aww.common.contants.PathConstant;
import com.bixin.gameFi.aww.service.IBOBMarketService;
import com.bixin.gameFi.aww.service.Impl.BOBMarketImpl;
import com.bixin.gameFi.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * @Author renjian
 * @Date 2022/3/21 11:27
 */
@RestController
@RequestMapping(PathConstant.AWW_REQUEST_PATH_PREFIX + "/normal")
public class BOBNormalController {
    @Autowired
    IBOBMarketService bobMarketService;

    /**
     * mint时获取单个nft的属性
     * todo：是否集群?需要加分布式锁?
     * @param account
     * @return
     */
    @GetMapping("mintInfo")
    public R getMintInfo(@RequestParam(required = false) String account) {
        R r = R.success(bobMarketService.getBOBMintInfo(account));
        return r;
    }

    /**
     * 获取nft封面链接
     * @param id
     * @return
     */
    @GetMapping("getImage/{id}")
    public R getImage(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response)  {
        String imageURL = bobMarketService.getNFTImage(id);
        try {
            response.sendRedirect(imageURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success();
    }

    /**
     * 查询单个用户普通场门票（还未报名的）
     * @param account
     * @return
     */
    @GetMapping("normalTicket")
    public R getBOBNormalTicket(@RequestParam String account, @RequestParam String raceType ) {
        R r = R.success(bobMarketService.getNormalTicket(account, raceType));
        return r;
    }

    /**
     * 普通场的场次信息
     *
     * @return
     */
    @GetMapping("text")
    public R getBOBResoruces() {
        R r = R.success(bobMarketService.getBOBConfig());
        return r;
    }

    /**
     *竞赛信息
     * @return
     */
    @GetMapping("raceInfo")
    public R getBOBReceInfo(@RequestParam String account) {
        R r = R.success(bobMarketService.getBOBRaceInfo(account));
        return r;
    }

    /**
     *被淘汰NFT列表
     * @return
     */
    @GetMapping("fallen")
    public R getBOBFallenInfo(@RequestParam(required = false) String account) {
        R r = R.success(bobMarketService.getBOBFallenInfo(account));
        return r;
    }


    /**
     *查询当前轮次我的已参赛NFT列表
     * @return
     */
    @GetMapping("mySignedNFT")
    public R getMySignedNFT(@RequestParam(required = false) String account) {
        R r = R.success(bobMarketService.getMySignedNFT(account));
        return r;
    }

    /**
     *查询当前轮次我的已参赛NFT列表
     * @return
     */
    @GetMapping("otherNFT")
    public R getOtherNFT(@RequestParam String account) {
        R r = R.success(bobMarketService.getOtherNFT(account));
        return r;
    }

}
