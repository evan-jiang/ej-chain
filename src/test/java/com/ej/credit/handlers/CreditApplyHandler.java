package com.ej.credit.handlers;

import com.ej.chain.annotation.ToContext;
import com.ej.chain.handlers.ProcessHandler;
import com.ej.credit.dto.CreditRequest;
import com.ej.credit.dto.CreditResponse;

public abstract class CreditApplyHandler implements ProcessHandler<CreditRequest> {
    @Override
    public boolean duplicated(CreditRequest creditRequest) {
        return false;
    }

    @Override
    public void process(CreditRequest creditRequest) {
        CreditResponse creditResponse = new CreditResponse();
        creditResponse.setProductCode(creditRequest.getProductCode());
        creditResponse.setApplyNo(creditRequest.getApplyNo());
        creditResponse.setApplyAmount(creditRequest.getApplyAmount());
        if (creditRequest.getApplyNo().endsWith("0")) {
            creditResponse.setStatus("SUCCESS");
        } else if (creditRequest.getApplyNo().endsWith("9")) {
            creditResponse.setStatus("FAIL");
        } else {
            throw new RuntimeException(creditRequest.getApplyNo() + " Error");
        }
        setCreditResponse(creditResponse);
    }

    @ToContext("creditResponse")
    abstract void setCreditResponse(CreditResponse creditResponse);
}
