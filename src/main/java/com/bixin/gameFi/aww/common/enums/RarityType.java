package com.bixin.gameFi.aww.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhangcheng
 * create  2021/12/8
 */
@Getter
@AllArgsConstructor
public enum RarityType {

    COMMON("common"),
    RARITY("rarity"),
    EPIC("epic"),
    CENTURY("century"),
    ONLY("only"),

    ;

    private String desc;

    public static RarityType of(String desc) {
        switch (desc) {
            case "common":
                return COMMON;
            case "rarity":
                return RARITY;
            case "epic":
                return EPIC;
            case "century":
                return CENTURY;
            case "only":
                return ONLY;
            default:
                return null;
        }
    }


}
