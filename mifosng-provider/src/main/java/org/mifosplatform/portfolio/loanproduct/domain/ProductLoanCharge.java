package org.mifosplatform.portfolio.loanproduct.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.mifosplatform.portfolio.charge.domain.Charge;

@Entity
@Table(name = "m_product_loan_charge")
public class ProductLoanCharge {
    @OneToMany
    @JoinColumn(name = "product_loan_id", nullable = false)
    private LoanProduct loanProduct;
    
    @OneToMany
    @JoinColumn(name = "charge_id", nullable = false)
    private Charge charge;
    
    @Column(name = "is_mandatory", nullable = false)
    private boolean isMandatory;
    
    
    public ProductLoanCharge(final LoanProduct loanProduct, final Charge charge, final boolean isMandatory) {
        this.loanProduct = loanProduct;
        this.charge = charge;
        this.isMandatory = isMandatory;
    }
}
