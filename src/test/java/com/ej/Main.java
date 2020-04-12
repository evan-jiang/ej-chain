package com.ej;

import com.alibaba.fastjson.JSON;
import com.ej.chain.context.ChainContext;
import com.ej.chain.dto.BaseResponse;
import com.ej.chain.handlers.Handler;
import com.ej.chain.proxy.HandlerProxy;
import com.ej.credit.dto.CreditRequest;
import com.ej.credit.dto.CreditResponse;
import com.ej.credit.handlers.CreditApplyHandler;
import com.ej.credit.handlers.CreditFinalHandler;
import com.ej.credit.handlers.CreditParamsCheckHandler;
import com.ej.manage.EjManage;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        int num = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        final CountDownLatch countDownLatch = new CountDownLatch(num);
        EjManage<CreditRequest, CreditResponse> ejManage = new EjManage<>();
        ejManage.register(new CreditParamsCheckHandler());
        ejManage.register(new HandlerProxy(CreditApplyHandler.class).getObject());
        ejManage.register(new HandlerProxy(CreditFinalHandler.class).getObject());
        for (int idx = 0; idx < num; idx++) {
            final String applyNo = String.valueOf(idx);
            executorService.execute(() -> {
                CreditRequest creditRequest = new CreditRequest();
                creditRequest.setProductCode("XY");
                creditRequest.setApplyNo(applyNo);
                creditRequest.setApplyAmount(new BigDecimal(500));
                BaseResponse<CreditResponse> baseResponse = ejManage.process(creditRequest);
                System.out.println(JSON.toJSONString(baseResponse));
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.gc();
        Thread.sleep(1000);
        System.gc();
        executorService.shutdown();


    }
}
