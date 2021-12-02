package com.bixin.gameFi.aww.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * nft出售事件 =  上架
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameFISellEventDto {
    // nft id
    private Long id;
    // 出售者
    private String seller;
    // 出售价格
    private BigDecimal selling_price;
    // 支付token
    private PayTokenCode pay_token_code;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class PayTokenCode {
        private String addr;
        private String module_name;
        private String name;
    }

//    public static NftEventDo of(GameFISellEventDto dto) {
//        NftEventDo.NftEventDoBuilder builder = NftEventDo.builder()
//                .nftId(dto.getId())
//                .creator("")
//                .seller(dto.getSeller())
//                .sellingPrice(dto.getSelling_price())
//                .bider("")
//                .bidPrice(BigDecimal.ZERO)
//                .type(NftEventType.NFT_SELL_EVENT.getDesc())
//                .createTime(LocalDateTimeUtil.getMilliByTime(LocalDateTime.now()))
//                .updateTime(LocalDateTimeUtil.getMilliByTime(LocalDateTime.now()));
//        PayTokenCode payTokenCode = dto.getPay_token_code();
//        String tokenCode = "";
//        if(!ObjectUtils.isEmpty(payTokenCode)){
//            tokenCode = payTokenCode.getAddr() + "::" + HexStringUtil.toStringHex(payTokenCode.getModule_name().replaceAll("0x",""))
//                    + "::"+ HexStringUtil.toStringHex(payTokenCode.getName().replaceAll("0x",""));
//        }
//        builder.payToken(tokenCode);
//        return builder.build();
//    }

}
