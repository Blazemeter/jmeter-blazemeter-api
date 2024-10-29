/**
 * Copyright 2018 BlazeMeter Inc.
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

package com.bzm.api.utils;

import com.bzm.api.exception.UnexpectedResponseException;
import com.bzm.api.explorer.CorrelationAsset;
import com.bzm.api.http.HttpUtils;
import java.io.IOException;
import java.util.List;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlazeMeterUtils extends HttpUtils {

    protected String address = BZM_ADDRESS;
    protected String dataAddress = BZM_DATA_ADDRESS;
    public static final String BZM_ADDRESS = "https://a.blazemeter.com";
    public static final String BZM_DATA_ADDRESS = "https://data.blazemeter.com";
    public static final String BZM_ASSETS_ADDRESS = "https://ar.blazemeter.com";

    private static final Logger LOG = LoggerFactory.getLogger(BlazeMeterUtils.class);
    /**
     * @param apiKeyId     - BlazeMeter Api Key Id
     * @param apiKeySecret - BlazeMeter Api Key Secret
     * @param address      - BlazeMeter app address: https://a.blazemeter.com/
     * @param dataAddress  - BlazeMeter data address: https://data.blazemeter.com/
     */
    public BlazeMeterUtils(String apiKeyId, String apiKeySecret) {
        super(apiKeyId, apiKeySecret);
    }

    @Override
    protected JSONObject processResponse(String response) {
        String error = extractErrorMessage(response);
        if (error != null) {
            LOG.error("Received response with the following error: " + error);
            throw new UnexpectedResponseException("Received response with the following error: " + error);
        }
        return JSONObject.fromObject(response);
    }

    @Override
    protected String extractErrorMessage(String response) {
        if (response != null && !response.isEmpty()) {
            try {
                JSONObject jsonResponse = JSONObject.fromObject(response);
                JSONObject errorObj = jsonResponse.getJSONObject("error");
                if (errorObj.containsKey("message")) {
                    return errorObj.getString("message");
                }
            } catch (JSONException ex) {
                LOG.debug("Cannot parse response: " + response, ex);
                return "Cannot parse response: " + response;
            }
        }
        return null;
    }

    public String getAddress() {
        return address;
    }

    public String getDataAddress() {
        return dataAddress;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDataAddress(String dataAddress) {
        this.dataAddress = dataAddress;
    }

    public static long getCheckTimeout() {
        try {
            return Long.parseLong(System.getProperty("bzm.checkTimeout", "10000"));
        } catch (NumberFormatException ex) {
            return 10000;
        }
    }

    public String getAssetUri(String workspaceId, String assetId) {
        return getAssetsAddress(workspaceId)+ "/"+ assetId + "/data";
    }

    public String getAssetsAddress(String workspaceId) {
        return BZM_ASSETS_ADDRESS + "/api/v1/workspaces/" + workspaceId + "/assets";
    }

    public String getAssetDataAddress(String workspaceId, String assetId) {
        return getAssetAddress(workspaceId, assetId) + "/data";
    }

    public String getAssetAddress(String workspaceId, String assetId) {
        return getAssetsAddress(workspaceId) + "/" + assetId;
    }

    public List<String> getUploadedFilesNames(String workspaceId) throws IOException {
        return CorrelationAsset.getUploadedFilesNames(this, workspaceId);
    }
}
