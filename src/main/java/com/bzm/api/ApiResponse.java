package com.bzm.api;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * This class is used to parse the response from the BlazeMeter API
 */
public class ApiResponse {

  private JSONObject jsonResponse;

  public ApiResponse(String jsonString) {
    this.jsonResponse = JSONObject.fromObject(jsonString);
  }

  public boolean hasErrors() {
    return jsonResponse.containsKey("error");
  }

  public String getErrorMessage() {
    if (hasErrors()) {
      return jsonResponse.getString("error");
    }
    return null;
  }

  public boolean hasResult() {
    return jsonResponse.containsKey("result");
  }

  public boolean isResultArray() {
    if (hasResult()) {
      return jsonResponse.get("result") instanceof JSONArray;
    }
    return false;
  }

  public JSONObject getResult() {
    if (hasResult()) {
      return jsonResponse.getJSONObject("result");
    }
    return null;
  }
}

