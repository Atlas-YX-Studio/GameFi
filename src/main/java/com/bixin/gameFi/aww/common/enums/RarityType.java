package com.bixin.gameFi.aww.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author zhangcheng
 * create  2021/12/8
 */
@Getter
@AllArgsConstructor
public enum RarityType {

    COMMON(1),
    RARITY(2),
    EPIC(3),
    CENTURY(4),
    ONLY(5),

    ;

    private Integer rarity;

    public static RarityType of(Integer rarity) {
        Optional<RarityType> typeOptional = Arrays.stream(RarityType.values()).filter(p -> Objects.equals(p.getRarity(), rarity)).findFirst();
        return typeOptional.isPresent() ? typeOptional.get() : null;
    }

}
