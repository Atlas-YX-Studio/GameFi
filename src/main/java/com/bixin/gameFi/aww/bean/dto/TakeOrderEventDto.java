package com.bixin.gameFi.aww.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zhangcheng
 * create  2021/12/7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TakeOrderEventDto {

    private long id;
    private String seller;
    private String buyer;
    private BigDecimal selling_price;
    private BigDecimal platform_fee;
    private TokenCode pay_token_code;
    private long time;

}
