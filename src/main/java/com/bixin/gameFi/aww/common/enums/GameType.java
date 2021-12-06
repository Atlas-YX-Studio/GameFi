package com.bixin.gameFi.aww.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhangcheng
 * create   2021/12/6
 */
@Getter
@AllArgsConstructor
public enum GameType {

    BUY_EVENT("ARMBuyEvent"),
    LIQUIDITY_EVENT("LiquidityEvent"),
    OFFLINE_EVENT("ARMOfflineEvent"),

    ;

    private String desc;

    public static GameType of(String desc) {
        switch (desc) {
            case "ARMBuyEvent":
                return BUY_EVENT;
            case "LiquidityEvent":
                return LIQUIDITY_EVENT;
            case "ARMOfflineEvent":
                return OFFLINE_EVENT;
            default:
                return null;
        }
    }

}
