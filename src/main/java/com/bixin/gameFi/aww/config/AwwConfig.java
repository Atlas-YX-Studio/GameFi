package com.bixin.gameFi.aww.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhangcheng
 * create   2021/12/6
 */
@Data
@Component
@ConfigurationProperties(prefix = "gamefi.aww")
public class AwwConfig {

    private Common common = new Common();
    private Websocket websocket = new Websocket();


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Common {
        private String url;
        private String contractAddress;
        private int chainId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Websocket {
        private String websocketHost;
        private String websocketPort;
    }

}
