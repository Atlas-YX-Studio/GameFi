package com.bixin.gameFi.aww.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author renjian
 * @Date 2022/3/21 11:50
 */
@Data
@Component
@ConfigurationProperties(prefix = "gamefi.bob")
public class BOBConfig {
    private BOBConfig.Common common = new BOBConfig.Common();
    private BOBConfig.Websocket websocket = new BOBConfig.Websocket();
    private BOBConfig.Content content = new BOBConfig.Content();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Common {
        private String contractAddress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Websocket {
        private String websocketHost;
        private String websocketPort;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String marketModule;
        private String armModule;
        private String awwModule;
        private String awwGameModule;
        private String grantModule;
        private String scriptsModule;

        private String nftName;
        private String titlePageImage;
        private String description;

        private Long imageId;
        private String imageData;
        private String nftMeta;
        private String nftBody;
        private String scripts;
        //        image-info-api: "https://test.kikoswap.com/v1/nft/image/group/1"
        private String imageInfoApi;
    }

}
