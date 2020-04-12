package com.ej.credit.dto;

import java.math.BigDecimal;

public class CreditResponse {

    private String productCode;
    private String applyNo;
    private BigDecimal applyAmount;
    private String status;

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getApplyNo() {
        return applyNo;
    }

    public void setApplyNo(String applyNo) {
        this.applyNo = applyNo;
    }

    public BigDecimal getApplyAmount() {
        return applyAmount;
    }

    public void setApplyAmount(BigDecimal applyAmount) {
        this.applyAmount = applyAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
