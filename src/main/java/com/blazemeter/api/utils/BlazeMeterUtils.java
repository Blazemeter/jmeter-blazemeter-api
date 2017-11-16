/**
 * Copyright 2017 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazemeter.api.utils;

import com.blazemeter.api.http.HttpUtils;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import okhttp3.Credentials;
import okhttp3.Request;
import org.apache.commons.lang.StringUtils;


public class BlazeMeterUtils extends HttpUtils {

    public static final String EMPTY_TOKEN = "";
    private final String token;

    protected final UserNotifier notifier;

    /**
     * @param apiKeyId     - BlazeMeter Api Key Id
     * @param apiKeySecret - BlazeMeter Api Key Secret
     * @param address      - BlazeMeter app address: http://a.blazemeter.com/
     * @param dataAddress  - BlazeMeter data address: http://data.blazemeter.com/
     * @param notifier     - user notifier, to show user information
     * @param logger       - logger, for log events of http requests / response etc.
     */
    public BlazeMeterUtils(String apiKeyId, String apiKeySecret,
                           String address, String dataAddress,
                           UserNotifier notifier, Logger logger) {
        super(address, dataAddress, logger);
        this.token = isValidCredantials(apiKeyId, apiKeySecret) ? Credentials.basic(apiKeyId, apiKeySecret) : EMPTY_TOKEN;
        this.notifier = notifier;
    }


    public BlazeMeterUtils(String address, String dataAddress, UserNotifier notifier, Logger logger) {
        this("", "", address, dataAddress, notifier, logger);
    }

    protected boolean isValidCredantials(String apiKeyId, String apiKeySecret) {
        return !StringUtils.isBlank(apiKeyId) && !StringUtils.isBlank(apiKeySecret);
    }

    @Override
    protected Request.Builder addRequiredHeader(Request.Builder requestBuilder) {
        return EMPTY_TOKEN.equals(token) ? requestBuilder : requestBuilder.addHeader(AUTHORIZATION, token);
    }

    @Override
    protected String extractErrorMessage(String response) {
        if (response != null && !response.isEmpty()) {
            try {
                JSON jsonResponse = JSONSerializer.toJSON(response, new JsonConfig());
                if (jsonResponse instanceof JSONObject) {
                    JSONObject object = (JSONObject) jsonResponse;
                    JSONObject errorObj = object.getJSONObject("error");
                    if (errorObj.containsKey("message")) {
                        return errorObj.getString("message");
                    }
                }
            } catch (JSONException ex) {
                logger.debug("Cannot parse JSON error response: " + response);
            }
        }
        return response;
    }
}
