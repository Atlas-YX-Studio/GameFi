package com.bixin.gameFi.aww.service.provider;

import com.bixin.gameFi.aww.bean.dto.PlaceOrderEventDto;
import com.bixin.gameFi.core.provider.IGameFiProvider;
import org.springframework.stereotype.Component;

/**
 * @author zhangcheng
 * create   2021/12/2
 */
@Component
public class AwwPlaceOrderProviderImpl implements IGameFiProvider<PlaceOrderEventDto> {

    @Override
    public void dispatcher(PlaceOrderEventDto placeOrderEventDto) {

    }

}