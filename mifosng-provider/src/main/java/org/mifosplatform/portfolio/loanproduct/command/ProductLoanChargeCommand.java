package org.mifosplatform.portfolio.loanproduct.command;



public class ProductLoanChargeCommand implements Comparable<ProductLoanChargeCommand> {
    
    @SuppressWarnings("unused")
    private final Long id;
    private final Long chargeId;
    private final Boolean isMandatory;
    
    
    public ProductLoanChargeCommand(final Long id, final Long chargeId, final Boolean isMandatory) {
        this.id = id;
        this.chargeId = chargeId;
        this.isMandatory = isMandatory;
    }
    
    @Override
    public int compareTo(final ProductLoanChargeCommand o) {
        int comparison = this.chargeId.compareTo(o.chargeId);
        if (comparison == 0) {
            comparison = this.isMandatory.compareTo(o.isMandatory);
        }
        return comparison;
    }

}
