/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.search.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.portfolio.group.service.SearchParameters;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.mifosplatform.portfolio.search.SearchConstants;
import org.mifosplatform.portfolio.search.data.AdHocQuerySearchConditions;
import org.mifosplatform.portfolio.search.data.AdHocSearchQueryData;
import org.mifosplatform.portfolio.search.data.SearchConditions;
import org.mifosplatform.portfolio.search.data.SearchData;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.client.domain.ClientEnumerations;
import org.mifosplatform.portfolio.group.domain.GroupingTypeEnumerations;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.mifosplatform.portfolio.loanaccount.data.LoanStatusEnumData;

@Service
public class SearchReadPlatformServiceImpl implements SearchReadPlatformService {

    private final NamedParameterJdbcTemplate namedParameterjdbcTemplate;
    private final PlatformSecurityContext context;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final PaginationHelper<AdHocSearchQueryData> paginationHelper = new PaginationHelper<AdHocSearchQueryData>();
    @Autowired
    public SearchReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final LoanProductReadPlatformService loanProductReadPlatformService, final OfficeReadPlatformService officeReadPlatformService) {
        this.context = context;
        this.namedParameterjdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
    }

    @Override
    public Collection<SearchData> retriveMatchingData(final SearchConditions searchConditions) {
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();

        final SearchMapper rm = new SearchMapper();

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("hierarchy", hierarchy + "%");
        params.addValue("search", searchConditions.getSearchQuery());
        params.addValue("partialSearch", "%" + searchConditions.getSearchQuery() + "%");

        return this.namedParameterjdbcTemplate.query(rm.searchSchema(searchConditions), params, rm);
    }

    private static final class SearchMapper implements RowMapper<SearchData> {

        public String searchSchema(final SearchConditions searchConditions) {

            final String union = " union ";
            final String clientExactMatchSql = " (select 'CLIENT' as entityType, c.id as entityId, c.display_name as entityName, c.external_id as entityExternalId, c.account_no as entityAccountNo "
                    + " , c.office_id as parentId, o.name as parentName, c.status_enum as entityStatusEnum "
                    + " from m_client c join m_office o on o.id = c.office_id where o.hierarchy like :hierarchy and (c.account_no like :search or c.display_name like :search or c.external_id like :search)) ";

            final String clientMatchSql = " (select 'CLIENT' as entityType, c.id as entityId, c.display_name as entityName, c.external_id as entityExternalId, c.account_no as entityAccountNo "
                    + " , c.office_id as parentId, o.name as parentName, c.status_enum as entityStatusEnum  "
                    + " from m_client c join m_office o on o.id = c.office_id where o.hierarchy like :hierarchy and (c.account_no like :partialSearch and c.account_no not like :search) or "
                    + "(c.display_name like :partialSearch and c.display_name not like :search) or "
                    + "(c.external_id like :partialSearch and c.external_id not like :search))";

            final String loanExactMatchSql = " (select 'LOAN' as entityType, l.id as entityId, pl.name as entityName, l.external_id as entityExternalId, l.account_no as entityAccountNo "
                    + " , c.id as parentId, c.display_name as parentName, l.loan_status_id as entityStatusEnum "
                    + " from m_loan l join m_client c on l.client_id = c.id join m_office o on o.id = c.office_id join m_product_loan pl on pl.id=l.product_id where o.hierarchy like :hierarchy and l.account_no like :search) ";

            final String loanMatchSql = " (select 'LOAN' as entityType, l.id as entityId, pl.name as entityName, l.external_id as entityExternalId, l.account_no as entityAccountNo "
                    + " , c.id as parentId, c.display_name as parentName, l.loan_status_id as entityStatusEnum "
                    + " from m_loan l join m_client c on l.client_id = c.id join m_office o on o.id = c.office_id join m_product_loan pl on pl.id=l.product_id where o.hierarchy like :hierarchy and l.account_no like :partialSearch and l.account_no not like :search) ";

            final String clientIdentifierExactMatchSql = " (select 'CLIENTIDENTIFIER' as entityType, ci.id as entityId, ci.document_key as entityName, "
                    + " null as entityExternalId, null as entityAccountNo, c.id as parentId, c.display_name as parentName, c.status_enum as entityStatusEnum "
                    + " from m_client_identifier ci join m_client c on ci.client_id=c.id join m_office o on o.id = c.office_id "
                    + " where o.hierarchy like :hierarchy and ci.document_key like :search) ";

            final String clientIdentifierMatchSql = " (select 'CLIENTIDENTIFIER' as entityType, ci.id as entityId, ci.document_key as entityName, "
                    + " null as entityExternalId, null as entityAccountNo, c.id as parentId, c.display_name as parentName, c.status_enum as entityStatusEnum "
                    + " from m_client_identifier ci join m_client c on ci.client_id=c.id join m_office o on o.id = c.office_id "
                    + " where o.hierarchy like :hierarchy and ci.document_key like :partialSearch and ci.document_key not like :search) ";

            final String groupExactMatchSql = " (select IF(g.level_id=1,'CENTER','GROUP') as entityType, g.id as entityId, g.display_name as entityName, g.external_id as entityExternalId, NULL as entityAccountNo "
                    + " , g.office_id as parentId, o.name as parentName, g.status_enum as entityStatusEnum "
                    + " from m_group g join m_office o on o.id = g.office_id where o.hierarchy like :hierarchy and (g.display_name like :search or g.external_id like :search)) ";

            final String groupMatchSql = " (select IF(g.level_id=1,'CENTER','GROUP') as entityType, g.id as entityId, g.display_name as entityName, g.external_id as entityExternalId, NULL as entityAccountNo "
                    + " , g.office_id as parentId, o.name as parentName, g.status_enum as entityStatusEnum "
                    + " from m_group g join m_office o on o.id = g.office_id where o.hierarchy like :hierarchy and (g.display_name like :partialSearch and g.display_name not like :search) or (g.external_id like :partialSearch and g.external_id not like :search)) ";

            final StringBuffer sql = new StringBuffer();

            // first include all exact matches
            if (searchConditions.isClientSearch()) {
                sql.append(clientExactMatchSql).append(union);
            }

            if (searchConditions.isLoanSeach()) {
                sql.append(loanExactMatchSql).append(union);
            }

            if (searchConditions.isClientIdentifierSearch()) {
                sql.append(clientIdentifierExactMatchSql).append(union);
            }

            if (searchConditions.isGroupSearch()) {
                sql.append(groupExactMatchSql).append(union);
            }

            // include all matching records
            if (searchConditions.isClientSearch()) {
                sql.append(clientMatchSql).append(union);
            }

            if (searchConditions.isLoanSeach()) {
                sql.append(loanMatchSql).append(union);
            }

            if (searchConditions.isClientIdentifierSearch()) {
                sql.append(clientIdentifierMatchSql).append(union);
            }

            if (searchConditions.isGroupSearch()) {
                sql.append(groupMatchSql).append(union);
            }

            sql.replace(sql.lastIndexOf(union), sql.length(), "");

            // remove last occurrence of "union all" string
            return sql.toString();
        }

        @Override
        public SearchData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long entityId = JdbcSupport.getLong(rs, "entityId");
            final String entityAccountNo = rs.getString("entityAccountNo");
            final String entityExternalId = rs.getString("entityExternalId");
            final String entityName = rs.getString("entityName");
            final String entityType = rs.getString("entityType");
            final Long parentId = JdbcSupport.getLong(rs, "parentId");
            final String parentName = rs.getString("parentName");
            final Integer entityStatusEnum = JdbcSupport.getInteger(rs, "entityStatusEnum");

            EnumOptionData entityStatus = new EnumOptionData(0L, "", "");

            if (entityType.equalsIgnoreCase("client") || entityType.equalsIgnoreCase("clientidentifier")) {
                entityStatus = ClientEnumerations.status(entityStatusEnum);
            }

            else if (entityType.equalsIgnoreCase("group") || entityType.equalsIgnoreCase("center")) {
                entityStatus = GroupingTypeEnumerations.status(entityStatusEnum);
            }

            else if (entityType.equalsIgnoreCase("loan")) {
                LoanStatusEnumData loanStatusEnumData = LoanEnumerations.status(entityStatusEnum);

                entityStatus = LoanEnumerations.status(loanStatusEnumData);
            }

            return new SearchData(entityId, entityAccountNo, entityExternalId, entityName, entityType, parentId, parentName, entityStatus,
                    null, null);

        }

    }

    @Override
    public AdHocSearchQueryData retrieveAdHocQueryTemplate() {

        this.context.authenticatedUser();

        final Collection<LoanProductData> loanProducts = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
        final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        return AdHocSearchQueryData.template(loanProducts, offices);
    }

    @Override
    public Collection<AdHocSearchQueryData> retrieveAdHocQueryMatchingDataSummary(final AdHocQuerySearchConditions searchConditions) {

        this.context.authenticatedUser();

        final AdHocQuerySearchMapper rm = new AdHocQuerySearchMapper();
        final MapSqlParameterSource params = new MapSqlParameterSource();

        return this.namedParameterjdbcTemplate.query(rm.schema(searchConditions, params), params, rm);
    }
    
    @Override
    public Page<AdHocSearchQueryData> retrieveAdHocQueryMatchingDataDetails(final AdHocQuerySearchConditions searchConditions,
            final SearchParameters searchParameters) {
        this.context.authenticatedUser();
        final AdHocQuerySearchMapper adHocQueryMapper = new AdHocQuerySearchMapper();
        final MapSqlParameterSource params = new MapSqlParameterSource();

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(adHocQueryMapper.fundMappingSchema(searchConditions, params));

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPageForNamedParameter(this.namedParameterjdbcTemplate, sqlCountRows, sqlBuilder.toString(),
                params, adHocQueryMapper);
    }

    private static final class AdHocQuerySearchMapper implements RowMapper<AdHocSearchQueryData> {

        private boolean isWhereClauseAdded = false;
        
        public String fundMappingSchema(final AdHocQuerySearchConditions searchConditions, final MapSqlParameterSource params) {
            final StringBuilder sql = new StringBuilder();
            final String queryType = "details";
            sql.append(
                    "a.display_name as clientName, a.name as officeName, a.productName, a.fundName, a.loanId, a.accountNo, a.fundType, a.disburseAmt, a.disburseOnDate, ")
                    .append("a.outstandingAmt as outstanding, a.percentOut as percentOut from (select mc.display_name, mo.name, mp.name as productName, ")
                    .append("ml.id as loanId, ml.account_no as accountNo, mcv.code_value as fundType, ")
                    .append("mf.name as fundName, (ifnull(ml.principal_disbursed_derived,0.0)) disburseAmt, ml.disbursedon_date as disburseOnDate, ")
                    .append("(ifnull(ml.total_outstanding_derived,0.0)) outstandingAmt,  ")
                    .append("((ifnull(ml.total_outstanding_derived,0.0)) * 100 / (ifnull(ml.principal_disbursed_derived,0.0))) percentOut ")
                    .append("from m_loan ml inner join m_product_loan mp on mp.id=ml.product_id  inner join m_client mc on  ")
                    .append("mc.id=ml.client_id left join m_fund mf on mf.id=ml.fund_id left join m_code_value mcv on mcv.id = ml.fund_type_cv_id inner join m_office mo on mo.id=mc.office_id  ");
            return addConditions(sql, searchConditions, params, queryType);
        }
        //TODO- build the query dynamically based on selected entity types, for now adding query for only loan entity.
        public String schema(final AdHocQuerySearchConditions searchConditions, final MapSqlParameterSource params) {
            final StringBuilder sql = new StringBuilder();
            sql.append("Select a.name as officeName, a.Product as productName, a.cnt as 'count', a.outstandingAmt as outstanding, a.percentOut as percentOut  ")
            .append("from (select mo.name, mp.name Product, sum(ifnull(ml.total_expected_repayment_derived,0.0)) TotalAmt, count(*) cnt, ")
            .append("sum(ifnull(ml.total_outstanding_derived,0.0)) outstandingAmt,  ")
            .append("(sum(ifnull(ml.total_outstanding_derived,0.0)) * 100 / sum(ifnull(ml.total_expected_repayment_derived,0.0))) percentOut ")
            .append("from m_loan ml inner join m_product_loan mp on mp.id=ml.product_id  ")
            .append("inner join m_client mc on mc.id=ml.client_id  ")
            .append("inner join m_office mo on mo.id=mc.office_id  ");
            
            return addConditions(sql, searchConditions, params, null);
        }
        
        public String addConditions(final StringBuilder sql, final AdHocQuerySearchConditions searchConditions, 
        		final MapSqlParameterSource params, final String queryType) {    
            if (searchConditions.getLoanStatus() != null && searchConditions.getLoanStatus().size() > 0) {
                // If user requests for all statuses no need to add loanStatus
                // filter
                if (!searchConditions.getLoanStatus().contains("all")) {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("loanStatus", searchConditions.getLoanStatus());
                    sql.append(" ml.loan_status_id in (:loanStatus) ");
                }
            }

            if (searchConditions.getLoanProducts() != null && searchConditions.getLoanProducts().size() > 0) {
                checkAndUpdateWhereClause(sql);
                params.addValue("loanProducts", searchConditions.getLoanProducts());
                sql.append(" mp.id in (:loanProducts) ");
            }

            if (searchConditions.getOffices() != null && searchConditions.getOffices().size() > 0) {
                checkAndUpdateWhereClause(sql);
                params.addValue("offices", searchConditions.getOffices());
                sql.append(" mo.id in (:offices) ");
            }

            if (StringUtils.isNotBlank(searchConditions.getLoanDateOption())) {
                if (searchConditions.getLoanDateOption().equals(SearchConstants.SEARCH_LOAN_DATE.APPROVAL_DATE.getValue())) {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("loanFromDate", searchConditions.getLoanFromDate().toDate());
                    params.addValue("loanToDate", searchConditions.getLoanToDate().toDate());
                    sql.append(" ( ml.approvedon_date between :loanFromDate and :loanToDate ) ");
                } else if (searchConditions.getLoanDateOption().equals(SearchConstants.SEARCH_LOAN_DATE.CREATED_DATE.getValue())) {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("loanFromDate", searchConditions.getLoanFromDate().toDate());
                    params.addValue("loanToDate", searchConditions.getLoanToDate().toDate());
                    sql.append(" ( ml.submittedon_date between :loanFromDate and :loanToDate ) ");
                } else if (searchConditions.getLoanDateOption().equals(SearchConstants.SEARCH_LOAN_DATE.DISBURSAL_DATE.getValue())) {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("loanFromDate", searchConditions.getLoanFromDate().toDate());
                    params.addValue("loanToDate", searchConditions.getLoanToDate().toDate());
                    sql.append(" ( ml.disbursedon_date between :loanFromDate and :loanToDate ) ");
                }
            }
            
            if (queryType != "details") {
                sql.append(" group by mo.id) a ");
            } else {
                sql.append(" ) a ");
            }
            
            //update isWhereClauseAdded to false to add filters for derived table
            isWhereClauseAdded = false;

            if (searchConditions.getIncludeOutStandingAmountPercentage()) {
                if (searchConditions.getOutStandingAmountPercentageCondition().equals("between")) {
                    checkAndUpdateWhereClause(sql);
                    // params.addValue("outStandingAmountPercentageCondition",
                    // searchConditions.getOutStandingAmountPercentageCondition());
                    params.addValue("minOutStandingAmountPercentage", searchConditions.getMinOutStandingAmountPercentage());
                    params.addValue("maxOutStandingAmountPercentage", searchConditions.getMaxOutStandingAmountPercentage());
                    sql.append(" ( a.percentOut between :minOutStandingAmountPercentage and :maxOutStandingAmountPercentage ) ");
                } else {
                    checkAndUpdateWhereClause(sql);
                    // params.addValue("outStandingAmountPercentageCondition",
                    // searchConditions.getOutStandingAmountPercentageCondition());
                    params.addValue("outStandingAmountPercentage", searchConditions.getOutStandingAmountPercentage());
                    sql.append(" a.percentOut ").append(searchConditions.getOutStandingAmountPercentageCondition())
                            .append(" :outStandingAmountPercentage ");
                }
            }

            if (searchConditions.getIncludeOutstandingAmount()) {
                if (searchConditions.getOutstandingAmountCondition().equals("between")) {
                    checkAndUpdateWhereClause(sql);
                    // params.addValue("outstandingAmountCondition",
                    // searchConditions.getOutstandingAmountCondition());
                    params.addValue("minOutstandingAmount", searchConditions.getMinOutstandingAmount());
                    params.addValue("maxOutstandingAmount", searchConditions.getMaxOutstandingAmount());
                    sql.append(" ( a.outstandingAmt between :minOutstandingAmount and :maxOutstandingAmount ) ");
                } else {
                    checkAndUpdateWhereClause(sql);
                    // params.addValue("outstandingAmountCondition",
                    // searchConditions.getOutstandingAmountCondition());
                    params.addValue("outstandingAmount", searchConditions.getOutstandingAmount());
                    sql.append(" a.outstandingAmt ").append(searchConditions.getOutstandingAmountCondition())
                            .append(" :outstandingAmount ");
                }
            }

            return sql.toString();
        }
        
        private void checkAndUpdateWhereClause(final StringBuilder sql){
            if (isWhereClauseAdded) {
                sql.append(" and ");
            } else {
                sql.append(" where ");
                isWhereClauseAdded = true;
            }
        }

        @SuppressWarnings("null")
        @Override
        public AdHocSearchQueryData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            List<String> columnNames = new ArrayList<String>();
            if (rs != null) {
                ResultSetMetaData columns = rs.getMetaData();
                int i = 0;
                while (i < columns.getColumnCount()) {
                    i++;
                    columnNames.add(columns.getColumnLabel(i));
                }
            }

            String clientName = null;
            String fundName = null;
            BigDecimal disburseAmount = null;
            LocalDate disburseOnDate = null;
            Integer loanId = null;
            String accountNo = null;
            String fundType = null;
            
            if (columnNames.contains("clientName")) {
                clientName = rs.getString("clientName");
            }

            if (columnNames.contains("fundName")) {
                fundName = rs.getString("fundName");
            }

            if (columnNames.contains("disburseAmt")) {
                disburseAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "disburseAmt");
            }

            if (columnNames.contains("disburseOnDate")) {
                disburseOnDate = JdbcSupport.getLocalDate(rs, "disburseOnDate");
            }

            if (columnNames.contains("loanId")) {
                loanId = JdbcSupport.getInteger(rs, "loanId");
            }
            
            if (columnNames.contains("accountNo")) {
            	accountNo = rs.getString("accountNo");
            }
            
            if (columnNames.contains("fundType")) {
            	fundType = rs.getString("fundType");
            }

            final String officeName = rs.getString("officeName");
            final String loanProductName = rs.getString("productName");


            Integer count = null;
            BigDecimal loanOutStanding = null;

            if (columnNames.contains("count")) {
                count = JdbcSupport.getInteger(rs, "count");
            }

            if (columnNames.contains("outstanding")) {
                loanOutStanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "outstanding").setScale(2, RoundingMode.HALF_UP);
            }

            final Double percentage = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "percentOut").setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
            return AdHocSearchQueryData.matchedResult(clientName, officeName, loanProductName, fundName, count, disburseAmount,
                    disburseOnDate, loanOutStanding, percentage, loanId, accountNo, fundType);
        }

    }

}