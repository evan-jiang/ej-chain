package com.ej.manage;

import com.ej.chain.manages.AbstractManage;
import com.ej.enums.ErrorEnum;

public class EjManage<Request,Data> extends AbstractManage<Request,Data> {
    @Override
    public String systemErrorCode() {
        return ErrorEnum.SYSTEM_ERROR.getErrorCode();
    }

    @Override
    public String systemErrorMsg() {
        return ErrorEnum.SYSTEM_ERROR.getErrorMsg();
    }

    @Override
    public String successCode() {
        return ErrorEnum.SUCCESS.getErrorCode();
    }

    @Override
    public String successMsg() {
        return ErrorEnum.SUCCESS.getErrorMsg();
    }
}
