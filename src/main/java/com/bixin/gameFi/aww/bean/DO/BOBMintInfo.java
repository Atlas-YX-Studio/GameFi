package com.bixin.gameFi.aww.bean.DO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @Author renjian
 * @Date 2022/3/21 21:02
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BOBMintInfo {
    /**
     * 主键id
     */
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 图片链接
     */
    private String imageLink;

    private Integer state;

    private String description;

    private String account;
}
