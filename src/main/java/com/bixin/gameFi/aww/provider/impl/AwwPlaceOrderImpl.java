package com.bixin.gameFi.aww.provider.impl;

import com.bixin.gameFi.aww.bean.dto.PlaceOrderEventDto;
import com.bixin.gameFi.aww.bean.dto.TakeOrderEventDto;
import com.bixin.gameFi.aww.provider.IGameFiProvider;
import org.springframework.stereotype.Component;

/**
 * @author zhangcheng
 * create   2021/12/2
 */
@Component
public class AwwPlaceOrderImpl implements IGameFiProvider<PlaceOrderEventDto> {
    @Override
    public void dispatcher(PlaceOrderEventDto placeOrderEventDto) {

    }
}
