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

package org.openhubframework.openhub.admin.web.log;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import org.openhubframework.openhub.admin.services.log.LogEvent;
import org.openhubframework.openhub.admin.services.log.LogParser;
import org.openhubframework.openhub.admin.services.log.LogParserConfig;
import org.openhubframework.openhub.admin.services.log.LogParserConstants;
import org.openhubframework.openhub.admin.web.common.editor.LogbackIso8601DateTimeEditor;


/**
 * Controller that encapsulates actions around logs.
 */
@Controller
@RequestMapping("/log")
public class LogController {

    private static final Logger LOG = LoggerFactory.getLogger(LogController.class);

    @Autowired
    private LogParser logParser;

    @RequestMapping("/")
    public String getLogSearch(@RequestParam(value = "fromDate", required = false) OffsetDateTime fromDate,
                               @RequestParam MultiValueMap<String, String> params,
                               Model model) throws UnsupportedEncodingException {

        if (fromDate != null) {
            params.remove("fromDate");
            // remove empty values:
            for (List<String> valueList : params.values()) {
                ListIterator<String> values = valueList.listIterator();
                while (values.hasNext()) {
                    if (!StringUtils.hasText(values.next())) {
                        values.remove();
                    }
                }
            }
            model.mergeAttributes(params);
            return "redirect:" + UriUtils.encodePath(fromDate.format(LogParserConstants.LOGBACK_ISO8601_FORMATTER), "UTF-8");
        }

        OffsetDateTime from = OffsetDateTime.now().minusHours(2).truncatedTo(ChronoUnit.HOURS);
        model.addAttribute("fromDate", from.format(LogParserConstants.LOGBACK_ISO8601_FORMATTER));

        LogParserConfig logParserConfig = new LogParserConfig();
        logParserConfig.setGroupBy(LogParserConstants.DEFAULT_GROUP_BY_PROPERTY);
        logParserConfig.setGroupLimit(LogParserConstants.DEFAULT_GROUP_SIZE);
        model.addAttribute("config", logParserConfig);

        return "logSearch";
    }

    @RequestMapping("/{fromDate}")
    public String getLogOverview(
            @PathVariable("fromDate") OffsetDateTime fromDate,
            @RequestParam(value = LogParserConstants.VIEW_REQUEST_PARAMETER, required = false) String view,
            @RequestParam(value = LogParserConstants.GROUP_BY_REQUEST_PARAMETER, required = false) Set<String> groupBy,
            @RequestParam(value = LogParserConstants.GROUP_SIZE_REQUEST_PARAMETER, required = false) Integer groupSize,
            @RequestParam Map<String, String> params,
            Model model) {

        try {
            LogParserConfig logParserConfig = new LogParserConfig();
            logParserConfig.setFromDate(fromDate);
            logParserConfig.setGroupBy(groupBy);
            logParserConfig.setGroupLimit(groupSize);
            logParserConfig.setFilter(getSubProperties(params, "filter."));
            logParserConfig.setMsg(params.get("msg"));

            LOG.info("Looking for {}", logParserConfig.describe());

            File[] logFiles = logParser.getLogFiles(logParserConfig.getFromDate());
            Iterator<LogEvent> logEvents = (logFiles.length > 0)
                    ? logParser.getLogEventIterator(logParserConfig, Arrays.asList(logFiles))
                    : Collections.<LogEvent>emptyList().iterator();

            model.addAttribute("fromDate", fromDate);
            model.addAttribute("config", logParserConfig);
            model.addAttribute("logEvents", logEvents);
            model.addAttribute("view", view);
        } catch (IOException exc) {
            model.addAttribute("logErr", "Error occurred during reading log files:\n" + exc);
        }
        return "logByDate";
    }

    private Map<String, String> getSubProperties(Map<String, String> properties, String prefix) {
        Map<String, String> subProperties = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> property : properties.entrySet()) {
            if (property.getKey().startsWith(prefix)) {
                subProperties.put(property.getKey().substring(prefix.length()), property.getValue());
            }
        }
        return subProperties;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(OffsetDateTime.class, new LogbackIso8601DateTimeEditor());
    }
}
