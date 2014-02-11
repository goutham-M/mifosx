package org.mifosplatform.portfolio.fund.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.ObjectUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_fund_loan_mapping_history")
public class FundLoanMappingHistory extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
    
    @ManyToOne
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @ManyToOne
    @JoinColumn(name = "fund_type_cv_id", nullable = false)
    private CodeValue fundTypeCodeValue;

    @Column(name = "start_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    public static FundLoanMappingHistory createNewInstance(final Loan loan, final Fund fund, final CodeValue fundTypeCodeValue, final Date startDate) {
        return new FundLoanMappingHistory(loan, fund, fundTypeCodeValue, startDate, null);
    }

    protected FundLoanMappingHistory() {
        //
    }

    private FundLoanMappingHistory(final Loan loan, final Fund fund, final CodeValue fundTypeCodeValue, final Date startDate, final Date endDate) {
        this.loan = loan;
        this.fundTypeCodeValue = fundTypeCodeValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fund = fund;

    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public CodeValue getFundTypeCodeValue() {
        return this.fundTypeCodeValue;
    }
    
    public void updateFundTypeCodeValue(final CodeValue fundTypeCodeValue) {
        this.fundTypeCodeValue = fundTypeCodeValue;
    }
    
    public LocalDate getEndDate() {
        return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(this.endDate), null);
    }
}
