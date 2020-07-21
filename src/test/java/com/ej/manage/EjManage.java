package com.ej.manage;

import com.ej.chain.manages.AbstractManage;
import com.ej.enums.ErrorEnum;

public class EjManage<Request, Data> extends AbstractManage<Request, Data> {
    @Override
    protected String systemErrorCode() {
        return ErrorEnum.SYSTEM_ERROR.getErrorCode();
    }

    @Override
    protected String systemErrorMsg() {
        return ErrorEnum.SYSTEM_ERROR.getErrorMsg();
    }

    @Override
    protected String successCode() {
        return ErrorEnum.SUCCESS.getErrorCode();
    }

    @Override
    protected String successMsg() {
        return ErrorEnum.SUCCESS.getErrorMsg();
    }
}
