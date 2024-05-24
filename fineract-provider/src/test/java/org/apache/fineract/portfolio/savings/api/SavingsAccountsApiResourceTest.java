/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fineract.portfolio.savings.api;

import jakarta.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SavingsAccountsApiResourceTest {

    @Mock
    private ApiRequestParameterHelper parameterHelper;

    @Mock
    private SavingsAccountReadPlatformService readPlatformService;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private PlatformSecurityContext securityContext;

    @Mock
    private DefaultToApiJsonSerializer<SavingsAccountData> jsonSerializer;

    @InjectMocks
    private SavingsAccountsApiResource underTest;

    private ApiRequestJsonSerializationSettings apiRequestJsonSerializationSettings;

    @BeforeEach
    void setUp() throws IOException {
        apiRequestJsonSerializationSettings = new ApiRequestJsonSerializationSettings(false, null, false, false, false);
        given(parameterHelper.process(Mockito.any())).willReturn(apiRequestJsonSerializationSettings);
    }

    @Test
    void retrieveAllHasPermission() {
        AppUser appUser = Mockito.mock(AppUser.class);
        Page<SavingsAccountData> response = Mockito.mock(Page.class);
        // given
        given(readPlatformService.retrieveAll(any(SearchParameters.class))).willReturn(response);
        Mockito.doNothing().when(appUser).validateHasReadPermission("savingsaccount");
        given(securityContext.authenticatedUser()).willReturn(appUser);
        //when
        underTest.retrieveAll(uriInfo, null, null, null, null, null, null, "02041981");
        //then
        verify(readPlatformService, Mockito.times(1)).retrieveAll(any(SearchParameters.class));
        verify(jsonSerializer, Mockito.times(1)).serialize(apiRequestJsonSerializationSettings, response, SavingsApiSetConstants.SAVINGS_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @Test
    void retrieveAllHasNoPermission() {
        AppUser appUser = Mockito.mock(AppUser.class);
        // given
        Mockito.doThrow(NoAuthorizationException.class).when(appUser).validateHasReadPermission("savingsaccount");
        given(securityContext.authenticatedUser()).willReturn(appUser);
        //when
        assertThatThrownBy(() -> underTest.retrieveAll(uriInfo, null, null, null, null, null, null, "02041981")).isInstanceOf(NoAuthorizationException.class);
        //then
        verifyNoInteractions(readPlatformService);
    }
}
