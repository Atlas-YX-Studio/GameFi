package com.bixin.gameFi.aww.runner;

import com.alibaba.fastjson.JSONObject;
import com.bixin.gameFi.aww.config.BOBConfig;
import com.bixin.gameFi.aww.service.Impl.BOBMarketImpl;
import com.bixin.gameFi.common.factory.NamedThreadFactory;
import com.bixin.gameFi.core.contract.ContractBiz;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.novi.serde.Bytes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.stereotype.Component;
import org.starcoin.bean.ScriptFunctionObj;
import org.starcoin.utils.AccountAddressUtils;
import org.starcoin.utils.BcsSerializeHelper;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * @Author renjian
 * @Date 2022/3/22 21:11
 */
@Slf4j
@Component
public class BOBRacentRunner implements ApplicationRunner {
    @Autowired
    BOBMarketImpl bobMarketService;

    @Resource
    ContractBiz contractService;

    @Resource
    BOBConfig bobConfig;

    AtomicLong atomicSum = new AtomicLong(0);
    static final long initTime = 2000L;
    static final long initIntervalTime = 5000L;
    static final long maxIntervalTime = 60 * 1000L;
    ThreadPoolExecutor poolExecutor;
    static int parkMilliSeconds = 2000;

    @PostConstruct
    public void init() {
        poolExecutor = new ThreadPoolExecutor(1, 1, 0,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory("AwwEventConsumer-", true));
    }

    @PreDestroy
    public void destroy() {
        try {
            if (Objects.isNull(poolExecutor)) {
                return;
            }
            poolExecutor.shutdown();
            poolExecutor.awaitTermination(1, TimeUnit.SECONDS);
            log.info("AwwEventConsumerRunner ThreadPoolExecutor stopped");
        } catch (InterruptedException ex) {
            log.error("AwwEventConsumerRunner InterruptedException: ", ex);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        poolExecutor.execute(() -> process(args));
    }

    public void process(ApplicationArguments args) {
        String senderAddress = bobConfig.getCommon().getContractAddress();
        try {
            for (;;) {
                //当次轮训过后线程执行方式 0：立即下一次轮询 1：等待2s后进入下一次轮询 2：结束线程
                int threadState = dealRaceInfo(senderAddress);

                if (threadState ==1) {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(parkMilliSeconds));
                }else if (threadState == 2) {
                    break;//跳出for循环，结束当前线程
                }
            }
        }catch (Exception e) {
            long duration = initTime + (atomicSum.incrementAndGet() - 1) * initIntervalTime;
            duration = Math.min(duration, maxIntervalTime);
            log.error("BOBRacentRunner run exception count {}, next retry {}",
                    atomicSum.get(), duration, e);
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(duration));
            DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments("retry " + atomicSum.get());
            this.process(applicationArguments);
        }


    }

    private int dealRaceInfo(String senderAddress) throws Exception{
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Long currentTime = timestamp.getTime();
        //1、获取state.list_resource数据，todo：并更新缓存
        JSONObject raceInfo = bobMarketService.getBOBRaceInfo(null);
        int actualSurplus = raceInfo.getInteger("actual_surplus_count");//实际剩余人数
        int targetSuperplus = raceInfo.getInteger("target_surplus_count");//目标剩余人数

        //2、获取state值
        int state = raceInfo.getInteger("state");
        //3、如果state=0，则判断是否到达报名开始时间，如果到达报名开始时间，调用合约，将state改为1，合约调用过程要是同步等待，失败要写日志
        //4、如果state=1，则判断是否到达报名结束时间，如果到报名结束时间，调用合约，将state改为2，合约调用过程要是同步等待，没到达竞报名结束时间就继续下次轮询
        //5、如果state=2，说明竞赛开始了，则判断是否到达下次淘汰时间，如果到达下次淘汰时间，则调用合约，进行淘汰人数计算，合约调用过程要是同步等待
        //6、如果state=2或者3，并且还没有到达下次淘汰时间，则判断当前存活人数是否大于应该存活人数，如果大于，则需要调用合约进行淘汰，淘汰人数和淘汰哪些NFT也是同步调用合约
        //7、如果state=3，并且当前存活人数是否等于应该存活人数，结束线程

        int threadState = 0;

        switch (state) {
            case 0://代表当前竞赛为初始状态，判断是否到达报名开始时间，如果到达报名开始时间，调用合约，更新状态为报名中
                Long signUpStart = raceInfo.getLong("sign_up_start_ts");//报名开始时间
                if (currentTime >= signUpStart) {//todo:要改成大于等于-------------------
                    //todo：表示已经到达报名开始时间，需要调用合约,修改竞赛状态
                    List funArgs = Lists.newArrayList(

                    );
                    boolean executed = callFunction("f_set_state", funArgs);
                    if (!executed) {
                        log.error("当前普通场竞赛修改状态为报名开始失败。modulename：" + bobConfig.getContent().getNormalRaceModule() + "，function：f_set_state");
                        break;
                    }
                    log.info("当前普通场竞赛修改状态为报名开始成功。modulename：" + bobConfig.getContent().getNormalRaceModule() + "，function：f_set_state");
                    break;
                }else{
                    //线程等待2s后执行
                    threadState =  1;
                }
                break;
            case 1://代表当前竞赛为报名状态，判断是否到达报名截止时间，如果到达报名截止时间，调用合约，更新状态为竞赛中
                Long signUpEnd = raceInfo.getLong("sign_up_end_ts");//报名截止时间
                if (currentTime >= signUpEnd) {
                    //todo：表示已经到达截止开始时间，需要调用合约，修改竞赛状态
                    List funArgs = Lists.newArrayList(

                    );
                    boolean executed = callFunction("f_set_state", funArgs);
                    if (!executed) {
                        log.error("当前普通场竞赛修改状态为竞赛开始失败。modulename：" + bobConfig.getContent().getNormalRaceModule() + "，function：f_set_state");
                        break;
                    }
                    log.info("当前普通场竞赛修改状态为竞赛开始成功。modulename：" + bobConfig.getContent().getNormalRaceModule() + "，function：f_set_state");

                    break;
                }else {
                    //线程等待2s后执行
                    threadState = 1;
                }
                break;
            case 2://标识当前状态为竞赛中
                Long nextEliminate = raceInfo.getLong("next_eliminate_ts");
                //判断是否到达下次淘汰时间，如果到达则调用淘汰合约
                if (currentTime > nextEliminate) {
                    //todo:表示已经到达下次淘汰时间，需要调用淘汰合约
                    List funArgs = Lists.newArrayList(

                    );
                    boolean executed = callFunction("f_eliminate", funArgs);
                    if (!executed) {
                        log.error("当前普通场竞赛进入下一个淘汰轮次，调用合约失败。modulename：" + bobConfig.getContent().getNormalRaceModule() + "，function：f_eliminate");
                        break;
                    }
                    log.info("当前普通场竞赛进入下一个淘汰轮次，调用合约成功。modulename：" + bobConfig.getContent().getNormalRaceModule() + "，function：f_eliminate");


                }else {//没有到达下次淘汰时间，则要判断当前存活人数是否大于应该存活人数，如果大于则进行人数清理
                    if (actualSurplus > targetSuperplus) {
                        //调用合约，清理存活人数
                        List funArgs = Lists.newArrayList(
                                Bytes.valueOf(BcsSerializeHelper.serializeString(String.valueOf(currentTime)))
                        );
                        boolean executed = callFunction("f_clear", funArgs);
                        if (!executed) {
                            log.error("当前普通场竞赛轮次淘汰参赛者，调用合约失败。modulename：" + bobConfig.getContent().getNormalRaceModule() + "，function：f_clear");
                            break;
                        }
                        log.info("当前普通场竞赛轮次淘汰参赛者，调用合约成功。modulename：" + bobConfig.getContent().getNormalRaceModule() + "，function：f_clear");

                    }else {
                        //线程等待2s后执行
                        threadState = 1;
                    }
                }
                break;
            case 3://表示当前竞赛已结束，但有可能存活人数还未清理完
                //判断当前存活人数是否大于应该存活人数，如果大于则进行人数清理
                if (actualSurplus > targetSuperplus) {
                    //调用合约，清理存活人数
                    List funArgs = Lists.newArrayList(
                            Bytes.valueOf(BcsSerializeHelper.serializeString(String.valueOf(currentTime)))
                    );
                    boolean executed = callFunction("f_clear", funArgs);
                    if (!executed) {
                        log.error("当前普通场竞赛轮次淘汰参赛者，调用合约失败。modulename：" + bobConfig.getContent().getNormalRaceModule() + "，function：f_clear");
                        break;
                    }
                    log.info("当前普通场竞赛轮次淘汰参赛者，调用合约成功。modulename：" + bobConfig.getContent().getNormalRaceModule() + "，function：f_clear");

                }else {
                    //结束线程
                    threadState = 2;
                }
                break;
        }
        return threadState;
    }




    private boolean callFunction(String functionName, List funArgs) {

        String moduleName = bobConfig.getContent().getNormalRaceModule();
        String address = bobConfig.getCommon().getContractAddress();

        ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
                .builder()
                .moduleAddress(address)
                .moduleName(moduleName)
                .functionName(functionName)
                .args(funArgs)
                .tyArgs(Lists.newArrayList())
                .build();

        return contractService.callFunction(address, scriptFunctionObj);
    }
}
