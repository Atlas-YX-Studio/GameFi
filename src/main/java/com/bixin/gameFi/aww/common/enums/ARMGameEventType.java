package com.bixin.gameFi.aww.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhangcheng
 * create   2021/12/6
 */
@Getter
@AllArgsConstructor
public enum ARMGameEventType {

    PLACE_ORDER_EVENT("ARMPlaceOrderEvent"),
    TAKE_ORDER_EVENT("ARMTakeOrderEvent"),
    CANCEL_ORDER_EVENT("ARMCancelOrderEvent"),

    ;

    private String desc;

    public static ARMGameEventType of(String desc) {
        switch (desc) {
            case "ARMPlaceOrderEvent":
                return PLACE_ORDER_EVENT;
            case "ARMCancelOrderEvent":
                return CANCEL_ORDER_EVENT;
            case "ARMTakeOrderEvent":
                return TAKE_ORDER_EVENT;
            default:
                return null;
        }
    }

}
