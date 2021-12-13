package com.bixin.gameFi.aww.bean.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zhangcheng
 * create   2021/9/18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AwwChainMarketDto {

    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private Long id;
        private String seller;
        private BigDecimal selling_price;
        private List<ItemNFT> nft;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemNFT {
        private List<NFTVec> vec;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NFTVec {
        private long id;
        private String creator;
        private TypeMeta type_meta;
        private BodyMeta body;
    }

//    @Data
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class BaseMeta {
//        private String name;
//        private String image;
//        private String image_data;
//        private String "description";
//    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TypeMeta {
        private long rarity;
        private long stamina;
        private long win_rate_bonus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BodyMeta {
        private long time;
        private long used_stamina;
    }

}
