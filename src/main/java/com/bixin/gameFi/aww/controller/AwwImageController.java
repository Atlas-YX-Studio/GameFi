package com.bixin.gameFi.aww.controller;

import com.bixin.gameFi.aww.bean.DO.AwwArmInfo;
import com.bixin.gameFi.aww.common.contants.PathConstant;
import com.bixin.gameFi.aww.service.IAwwArmInfoService;
import com.bixin.gameFi.common.code.GameErrCode;
import com.bixin.gameFi.common.contants.CommonConstant;
import com.bixin.gameFi.common.exception.GameException;
import com.bixin.gameFi.common.response.R;
import com.bixin.gameFi.core.redis.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping(PathConstant.AWW_REQUEST_PATH_PREFIX + "/image")
public class AwwImageController {

    @Resource
    IAwwArmInfoService awwArmInfoService;

    @Resource
    RedisCache redisCache;

    @GetMapping("/arm/{id}")
    public R getInfoImage(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) {
        String url = (String) redisCache.getValue(CommonConstant.IMAGE_ARM_URL_PREFIX_KEY + id);
        if (StringUtils.isBlank(url)) {
            AwwArmInfo armInfo = awwArmInfoService.selectById(id);
            if (armInfo == null) {
                throw new GameException(GameErrCode.DATA_NOT_EXIST);
            }
            url = armInfo.getImageLink();
            if (StringUtils.isBlank(url)) {
                throw new GameException(GameErrCode.IMAGE_UPLOAD_FAILURE);
            }
            redisCache.setValue(CommonConstant.IMAGE_ARM_URL_PREFIX_KEY + id, url, 1, TimeUnit.HOURS);
        }
        try {
            if (StringUtils.equalsIgnoreCase(request.getHeader(CommonConstant.HTTP_X_REQUESTED_WITH),
                    CommonConstant.HTTP_AJAX_REQUEST_HEADER)) {
                return R.success(url);
            }
            response.sendRedirect(url);
            return R.success();
        } catch (IOException e) {
            throw new GameException(GameErrCode.DATA_NOT_EXIST);
        }
    }

}