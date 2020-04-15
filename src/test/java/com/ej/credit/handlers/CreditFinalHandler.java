package com.ej.credit.handlers;

import com.ej.chain.annotation.FromContext;
import com.ej.chain.exception.ChainForcedInterruptException;
import com.ej.chain.handlers.AbstractProcessHandler;
import com.ej.credit.dto.CreditRequest;
import com.ej.credit.dto.CreditResponse;
import com.ej.enums.ErrorEnum;

public abstract class CreditFinalHandler extends AbstractProcessHandler<CreditRequest> {
    @Override
    public boolean idempotent(CreditRequest creditRequest) {
        return false;
    }

    @Override
    public void process(CreditRequest creditRequest) {
        if (System.currentTimeMillis() >= 0) {
            throw new ChainForcedInterruptException(ErrorEnum.SYSTEM_ERROR.getErrorCode(),ErrorEnum.SYSTEM_ERROR.getErrorMsg());
        }
        injectData(getCreditResponse());
    }

    @FromContext("creditResponse")
    abstract CreditResponse getCreditResponse();

}
