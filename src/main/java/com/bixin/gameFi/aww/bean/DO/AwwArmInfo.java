package com.bixin.gameFi.aww.bean.DO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AwwArmInfo {
    private Long id;

    private Long armId;

    private String name;

    private Boolean rarity;

    private Boolean stamina;

    private Boolean winRateBonus;

    private String imageLink;

    private Boolean created;

    private Long createTime;

    private Long updateTime;

    private String imageData;

}