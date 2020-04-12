package com.ej.credit.handlers;

import com.ej.chain.annotation.FromContext;
import com.ej.chain.annotation.ToContext;
import com.ej.chain.handlers.AbstractProcessHandler;
import com.ej.credit.dto.Account;
import com.ej.credit.dto.CreditRequest;
import com.ej.credit.dto.CreditResponse;

public abstract class CreditFinalHandler extends AbstractProcessHandler<CreditRequest> {
    @Override
    public boolean idempotent(CreditRequest creditRequest) {
        return false;
    }

    @Override
    public void process(CreditRequest creditRequest) {
        injectData(getCreditResponse());
    }

    @FromContext("creditResponse")
    abstract CreditResponse getCreditResponse();

}
