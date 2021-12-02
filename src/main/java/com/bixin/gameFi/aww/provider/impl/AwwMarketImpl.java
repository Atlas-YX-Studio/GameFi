package com.bixin.gameFi.aww.provider.impl;

import com.bixin.gameFi.aww.bean.dto.GameFISellEventDto;
import com.bixin.gameFi.aww.provider.IGameFiProvider;
import org.springframework.stereotype.Component;

/**
 * @author zhangcheng
 * create   2021/12/2
 */
@Component
public class AwwMarketImpl implements IGameFiProvider<GameFISellEventDto> {
    @Override
    public void dispatcher(GameFISellEventDto gameFISellEventDto) {

    }
}
