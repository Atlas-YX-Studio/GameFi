package com.bixin.gameFi.aww.common.queue;

import com.bixin.gameFi.aww.common.contants.CommonConstant;
import com.bixin.gameFi.aww.common.enums.ARMGameEventType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zhangcheng
 * create          2021-08-12 11:54 上午
 */
@Slf4j
@Component
public class GameEventBlockingQueue {

    //订阅swap事件 队列
    public static Map<ARMGameEventType, LinkedBlockingQueue<JsonNode>> queueMap = new HashMap<>() {{
        put(ARMGameEventType.PLACE_ORDER_EVENT, new LinkedBlockingQueue<>(CommonConstant.GAME_EVENT_QUEUE_SIZE));
        put(ARMGameEventType.TAKE_ORDER_EVENT, new LinkedBlockingQueue<>(CommonConstant.GAME_EVENT_QUEUE_SIZE));
        put(ARMGameEventType.CANCEL_ORDER_EVENT, new LinkedBlockingQueue<>(CommonConstant.GAME_EVENT_QUEUE_SIZE));
    }};

}
