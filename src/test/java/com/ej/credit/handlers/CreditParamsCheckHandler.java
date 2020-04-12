package com.ej.credit.handlers;

import com.ej.chain.handlers.AbstractCheckHandler;
import com.ej.credit.dto.CreditRequest;
import com.ej.enums.ErrorEnum;

import java.math.BigDecimal;

public class CreditParamsCheckHandler extends AbstractCheckHandler<CreditRequest> {
    @Override
    public boolean checkParams(CreditRequest creditRequest) {
        if(creditRequest == null){
            injectError(ErrorEnum.PARAMS_ERROR.getErrorCode(),ErrorEnum.PARAMS_ERROR.buildErrorMsg("请求参数对象不能为空"));
            return false;
        }
        if(creditRequest.getProductCode() == null || creditRequest.getProductCode().trim().length() == 0){
            injectError(ErrorEnum.PARAMS_ERROR.getErrorCode(),ErrorEnum.PARAMS_ERROR.buildErrorMsg("请求参数productCode不能为空"));
            return false;
        }
        if(creditRequest.getApplyNo() == null || creditRequest.getApplyNo().trim().length() == 0){
            injectError(ErrorEnum.PARAMS_ERROR.getErrorCode(),ErrorEnum.PARAMS_ERROR.buildErrorMsg("请求参数applyNo不能为空"));
            return false;
        }
        if(creditRequest.getApplyAmount() == null){
            injectError(ErrorEnum.PARAMS_ERROR.getErrorCode(),ErrorEnum.PARAMS_ERROR.buildErrorMsg("请求参数applyAmount不能为空"));
            return false;
        }
        if(creditRequest.getApplyAmount().compareTo(new BigDecimal("500")) < 0){
            injectError(ErrorEnum.PARAMS_ERROR.getErrorCode(),ErrorEnum.PARAMS_ERROR.buildErrorMsg("请求参数applyAmount不能小于500"));
            return false;
        }
        if(creditRequest.getApplyAmount().compareTo(new BigDecimal("50000")) > 0){
            injectError(ErrorEnum.PARAMS_ERROR.getErrorCode(),ErrorEnum.PARAMS_ERROR.buildErrorMsg("请求参数applyAmount不能大于50000"));
            return false;
        }
        return true;
    }
}
