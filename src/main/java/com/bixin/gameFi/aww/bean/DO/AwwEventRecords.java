package com.bixin.gameFi.aww.bean.DO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AwwEventRecords {
    private Long id;

    private String eventType;

    private String detail;

    private Long createTime;
}