package com.bixin.gameFi.aww.bean.DO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AwwMatchRecords {
    private Long id;

    private String sellAddress;

    private String buyAddress;

    private Long awwId;

    private Long groupId;

    private String icon;

    private String groupName;

    private String awwName;

    private BigDecimal price;

    private BigDecimal fee;

    private Long matchTime;

    private Long createTime;

    private Long updateTime;

}