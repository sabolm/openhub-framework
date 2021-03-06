/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openhubframework.openhub.core.common.contextcall;

import static org.openhubframework.openhub.api.configuration.CoreProps.SERVER_LOCALHOST_URI;
import static org.openhubframework.openhub.api.route.RouteConstants.HTTP_URI_PREFIX;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import org.openhubframework.openhub.api.configuration.ConfigurableValue;
import org.openhubframework.openhub.api.configuration.ConfigurationItem;


/**
 * Implementation of {@link ContextCall} interface with HTTP client that calls {@link ContextCallRoute}.
 *
 * @author Petr Juza
 */
@Service
public class ContextCallHttpImpl extends AbstractContextCall {

    /**
     * URI of this localhost application, including port number.
     */
    @ConfigurableValue(key = SERVER_LOCALHOST_URI)
    private ConfigurationItem<String> localhostUri;

    @Override
    protected void callTargetMethod(String callId, Class<?> targetType, String methodName) {
        Assert.hasText(callId, "callId must not be empty");
        Assert.notNull(targetType, "targetType must not be null");
        Assert.hasText(methodName, "methodName must not be empty");
        Assert.hasText(localhostUri.getValue(), "localhostUri must not be empty");

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            HttpGet httpGet = new HttpGet(localhostUri.getValue() + HTTP_URI_PREFIX
                    + ContextCallRoute.SERVLET_URL + "?" + ContextCallRoute.CALL_ID_HEADER + "=" + callId);

            httpClient.execute(httpGet);
        } catch (IOException ex) {
            throw new IllegalStateException("error occurs during calling target method '" + methodName
                    + "' of service type '" + targetType.getSimpleName() + "'", ex);
        } finally {
            IOUtils.closeQuietly(httpClient);
        }
    }
}
