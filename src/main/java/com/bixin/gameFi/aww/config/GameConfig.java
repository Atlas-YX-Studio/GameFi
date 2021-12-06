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
public class GameConfig {

    private Event event = new Event();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Event {

        private String websocketHost;
        private String websocketPort;
        private String websocketContractAddress;

    }


}
