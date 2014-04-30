package org.mifosplatform.portfolio.loanproduct.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.loanproduct.command.ProductLoanChargeCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_product_loan_charge")
public class ProductLoanCharge extends AbstractPersistable<Long> {
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_loan_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(optional = false)
    @JoinColumn(name = "charge_id", nullable = false)
    private Charge charge;
    
    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory;
    
    
    public ProductLoanCharge() {
        // TODO Auto-generated constructor stub
    }
    
    public static ProductLoanCharge createNewFromJson(final Charge charge, final boolean isMandatory) {
        return new ProductLoanCharge(null, charge, isMandatory);
    }
    
    private ProductLoanCharge(final LoanProduct loanProduct, final Charge charge, final boolean isMandatory) {
        this.loanProduct = loanProduct;
        this.charge = charge;
        this.isMandatory = isMandatory;
    }
    
    public void update(final LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        final ProductLoanCharge rhs = (ProductLoanCharge) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)) //
                .append(getId(), rhs.getId()) //
                .append(this.charge.getId(), rhs.charge.getId()) //
                .append(this.isMandatory, rhs.isMandatory) //
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 5) //
                .append(getId()) //
                .append(this.charge.getId()) //
                .append(this.isMandatory) //
                .toHashCode();
    }
    
    public Boolean isMandatory() {
        return this.isMandatory;
    }
    
    public ProductLoanChargeCommand toCommand() {
        return new ProductLoanChargeCommand(getId(), this.charge.getId(), this.isMandatory);
    }
    
    public void update(Boolean isMandatory) {
        this.isMandatory = isMandatory;
    }
}
