package com.bixin.gameFi.aww.controller;

import com.bixin.gameFi.aww.common.contants.PathConstant;
import com.bixin.gameFi.aww.common.response.P;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangcheng
 * create  2021/12/9
 */
@RestController
@RequestMapping(PathConstant.AWW_REQUEST_PATH_PREFIX +"/market")
public class AwwMarketController {


    @GetMapping("/getALL")
    public P getALlByPage(@RequestParam(value = "groupId", defaultValue = "0") long groupId,
                          @RequestParam(value = "startPrice", defaultValue = "0") long startPrtice,
                          @RequestParam(value = "endPrice", defaultValue = "0") long endPrice,
                          @RequestParam(value = "sort", defaultValue = "0") int sort,
                          @RequestParam(value = "rarity", defaultValue = "all") String rarity,
                          @RequestParam(value = "pageSize", defaultValue = "20") long pageSize,
                          @RequestParam(value = "pageNum", defaultValue = "0") long pageNum) {

//        if (pageNum < 0 || pageSize <= 0 || groupId < 0
//                || StringUtils.isEmpty(open) || StringUtils.isEmpty(currency)) {
//            return P.failed("parameter is invalid");
//        }
//        List<Map<String, Object>> maps = nftMarketService.selectByPage(true, pageSize + 1, pageNum, sort, groupId, currency, open);
//        if (CollectionUtils.isEmpty(maps)) {
//            return P.success(null, false);
//        }
//
//        boolean hasNext = false;
//        if (maps.size() > pageSize) {
//            maps = maps.subList(0, maps.size() - 1);
//            hasNext = true;
//        }
//
//        List<NftSelfSellingVo> list = new ArrayList<>();
//        maps.stream().forEach(p -> list.add(NftSelfSellingVo.of(p)));
//
//        Set<Long> groupIds = list.stream().map(p -> p.getGroupId()).collect(Collectors.toSet());
//        Map<Long, NftGroupDo> map = new HashMap<>();
//        groupIds.forEach(id -> {
//            NftGroupDo nftGroupDo = nftGroupService.selectById(id);
//            map.put(id, nftGroupDo);
//        });
//
//        for (NftSelfSellingVo p : list) {
//            NftGroupDo nftGroupDo = map.get(p.getGroupId());
//            if (Objects.nonNull(nftGroupDo)) {
//                String boxToken = nftGroupDo.getBoxToken();
//                String nftMeta = nftGroupDo.getNftMeta();
//                String nftBody = nftGroupDo.getNftBody();
//                p.setBoxToken(boxToken);
//                p.setNftMeta(nftMeta);
//                p.setNftBody(nftBody);
//            }
//        }
//
//        return P.success(list, hasNext);

        return P.success();
    }

}
