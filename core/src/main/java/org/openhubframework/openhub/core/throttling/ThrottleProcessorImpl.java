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

package org.openhubframework.openhub.core.throttling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import org.openhubframework.openhub.api.exception.ThrottlingExceededException;
import org.openhubframework.openhub.spi.throttling.*;


/**
 * Implementation of {@link ThrottlingProcessor} interface.
 *
 * @author Petr Juza
 */
@Service
public class ThrottleProcessorImpl implements ThrottlingProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ThrottleProcessorImpl.class);

    @Autowired
    private ThrottlingConfiguration configuration;

    @Autowired
    private ThrottleCounter counter;

    @Override
    public void throttle(ThrottleScope throttleScope) {
        if (!configuration.isThrottlingDisabled()) {
            Assert.notNull(throttleScope, "throttleScope must not be null");

            Assert.isTrue(!throttleScope.getSourceSystem().equals(ThrottleScope.ANY_SOURCE_SYSTEM)
                || !throttleScope.getServiceName().equals(ThrottleScope.ANY_SERVICE),
                    "throttle scope must define source system or service name (one of them at least)");


            ThrottleProps throttleProps = configuration.getThrottleProps(throttleScope);
            if (throttleProps == null) {
                LOG.warn("no throttling for input request: " + throttleScope);
                return;
            }

            int reqCount = counter.count(throttleScope, throttleProps.getInterval());

            if (reqCount > throttleProps.getLimit()) {
                String errMsg = "Actual count of requests for source system '" + throttleScope.getSourceSystem()
                        + "' and service '" + throttleScope.getServiceName()
                        + "' exceeded limit (interval=" + throttleProps.getInterval()
                        + "sec, limit=" + throttleProps.getLimit() + ", actual count=" + reqCount + ")";

                LOG.warn(errMsg);

                throw new ThrottlingExceededException(errMsg);
            }
        }
    }
}
