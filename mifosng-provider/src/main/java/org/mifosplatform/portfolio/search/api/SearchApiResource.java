/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.search.api;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.portfolio.fund.data.FundData;
import org.mifosplatform.portfolio.fund.service.FundReadPlatformService;
import org.mifosplatform.portfolio.group.service.SearchParameters;
import org.mifosplatform.portfolio.search.SearchConstants.SEARCH_RESPONSE_PARAMETERS;
import org.mifosplatform.portfolio.search.data.AdHocQueryDataValidator;
import org.mifosplatform.portfolio.search.data.AdHocQuerySearchConditions;
import org.mifosplatform.portfolio.search.data.AdHocSearchQueryData;
import org.mifosplatform.portfolio.search.data.SearchConditions;
import org.mifosplatform.portfolio.search.data.SearchData;
import org.mifosplatform.portfolio.search.service.SearchReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/search")
@Component
@Scope("singleton")
public class SearchApiResource {

    private final Set<String> searchResponseParameters = SEARCH_RESPONSE_PARAMETERS.getAllValues();

    private final SearchReadPlatformService searchReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final ToApiJsonSerializer<Object> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final AdHocQueryDataValidator fromApiJsonDeserializer;

    @Autowired
    public SearchApiResource(final SearchReadPlatformService searchReadPlatformService,
            final ToApiJsonSerializer<Object> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final AdHocQueryDataValidator fromApiJsonDeserializer, final FundReadPlatformService fundReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService) {

        this.searchReadPlatformService = searchReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.fundReadPlatformService = fundReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;

    }
    
    @GET
    @Path("/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAdHocSearchQueryTemplate(@Context final UriInfo uriInfo, 
			@DefaultValue("false") @QueryParam("fundMapTemplate") final Boolean fundMapTemplate) {
    	final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
    	if (fundMapTemplate) {
    		final Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
    		final Collection<CodeValueData> fundTypeOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("fundType");
    		SearchData searchDataTemplate = SearchData.searchDataInstance(fundOptions, fundTypeOptions);
    		return this.toApiJsonSerializer.serialize(settings, searchDataTemplate);
    	}
    	
        final AdHocSearchQueryData templateData = this.searchReadPlatformService.retrieveAdHocQueryTemplate();
        
        return this.toApiJsonSerializer.serialize(settings, templateData);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String searchData(@Context final UriInfo uriInfo, @QueryParam("query") final String query,
            @QueryParam("resource") final String resource) {

        final SearchConditions searchConditions = new SearchConditions(query, resource);

        final Collection<SearchData> searchResults = this.searchReadPlatformService.retriveMatchingData(searchConditions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, searchResults, this.searchResponseParameters);
    }
    
    @POST
    @Path("/advance")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String advancedSearch(@Context final UriInfo uriInfo,final String json, @QueryParam("queryType") final String queryType,
    		@QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit) {
        
        final AdHocQuerySearchConditions searchConditions = this.fromApiJsonDeserializer.retrieveSearchConditions(json);
        final SearchParameters searchParameters = SearchParameters.adHocQuery(offset, limit);
        
        Collection<AdHocSearchQueryData> searchResultSummary = null;
        Page<AdHocSearchQueryData> searchResultDetails = null;
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (queryType.equals("summary")) {
        	searchResultSummary = this.searchReadPlatformService.retrieveAdHocQueryMatchingDataSummary(searchConditions);
        	return this.toApiJsonSerializer.serialize(settings, searchResultSummary);
        } 
        
    	searchResultDetails = this.searchReadPlatformService.retrieveAdHocQueryMatchingDataDetails(searchConditions, searchParameters);
    	return this.toApiJsonSerializer.serialize(settings, searchResultDetails);
    }
}