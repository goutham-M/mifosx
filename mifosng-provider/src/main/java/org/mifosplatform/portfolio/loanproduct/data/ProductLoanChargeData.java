package org.mifosplatform.portfolio.loanproduct.data;


public class ProductLoanChargeData {

    private final Long id;
    @SuppressWarnings("unused")
    private final Long loanId;
    @SuppressWarnings("unused")
    private final Long chargeId;
    private Boolean isMandatory;
    
    public ProductLoanChargeData(final Long id, boolean isMandatory) {
        this.id = id;
        this.loanId = null;
        this.chargeId = null;
        this.isMandatory = isMandatory;
    }
    
    public Long getId() {
        return this.id;
    }
    
    public Boolean getIsMandatory() {
        return this.isMandatory;
    }
}
