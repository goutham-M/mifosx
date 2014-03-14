package org.mifosplatform.portfolio.fund.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FundLoanMappingHistoryRepository extends JpaRepository<FundLoanMappingHistory, Long>, JpaSpecificationExecutor<FundLoanMappingHistory> {
	
	@Query("from FundLoanMappingHistory  fundmap where fundmap.loan.id = :loanId and fundmap.endDate = null")
	FundLoanMappingHistory loanAccountFundMapChanged(@Param("loanId") Long loanId);
}
