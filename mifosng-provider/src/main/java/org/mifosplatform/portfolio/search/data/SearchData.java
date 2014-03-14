/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.search.data;

import java.util.Collection;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.portfolio.fund.data.FundData;

public class SearchData {

    private final Long entityId;
    private final String entityAccountNo;
    private final String entityExternalId;
    private final String entityName;
    private final String entityType;
    private final Long parentId;
    private final String parentName;
    @SuppressWarnings("unused")
	private final Collection<FundData> fundOptions;
    @SuppressWarnings("unused")
	private final Collection<CodeValueData> fundTypeOptions;

    public SearchData(final Long entityId, final String entityAccountNo, final String entityExternalId, final String entityName,
            final String entityType, final Long parentId, final String parentName, final Collection<FundData> fundOptions,
            final Collection<CodeValueData> fundTypeOptions) {

        this.entityId = entityId;
        this.entityAccountNo = entityAccountNo;
        this.entityExternalId = entityExternalId;
        this.entityName = entityName;
        this.entityType = entityType;
        this.parentId = parentId;
        this.parentName = parentName;
        this.fundOptions = fundOptions;
        this.fundTypeOptions = fundTypeOptions;
    }
    
    public static SearchData searchDataInstance(final Collection<FundData> fundOptions, Collection<CodeValueData> fundTypeOptions) {
    	return new SearchData(null, null, null, null, null, null, null, fundOptions, fundTypeOptions);
    }
    
    public Long getEntityId() {
        return this.entityId;
    }

    public String getEntityAccountNo() {
        return this.entityAccountNo;
    }

    public String getEntityExternalId() {
        return this.entityExternalId;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public String getEntityType() {
        return this.entityType;
    }

    public Long getParentId() {
        return this.parentId;
    }

    public String getParentName() {
        return this.parentName;
    }

}
