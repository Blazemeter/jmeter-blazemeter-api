package com.bzm.api.utils;

import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockedBlazeMeterUtils extends BlazeMeterUtils {
  private Map<String, String> mockedResponses = new HashMap<>();
  private List<String> mockedRequests = new ArrayList<>();
  private int mockedRequestCount = 0;

  private List<String> normalRequests = new ArrayList<>();
  private int normalRequestCount = 0;

  /**
   * @param apiKeyId     - BlazeMeter Api Key Id
   * @param apiKeySecret - BlazeMeter Api Key Secret
   */
  public MockedBlazeMeterUtils(String apiKeyId, String apiKeySecret) {
    super(apiKeyId, apiKeySecret);
  }

  public void addMockedResponse(String url, String responsePath) {
    mockedResponses.put(url, responsePath);
  }

  public void clearMockedResponses() {
    mockedResponses.clear();
    mockedRequests.clear();
    normalRequests.clear();
    mockedRequestCount = 0;
    normalRequestCount = 0;
  }


  @Override
  public String executeRequest(HttpsURLConnection connection) throws IOException {
    String urlString = connection.getURL().toString();
    if (mockedResponses.containsKey(urlString)) {
      mockedRequestCount++;
      mockedRequests.add(urlString);
      String responsePath = mockedResponses.get(urlString);
      return getContentFromFilePath(responsePath);
    }
    normalRequestCount++;
    normalRequests.add(urlString);
    return super.executeRequest(connection);
  }

  private String getContentFromFilePath(String filePath) throws IOException {
    File file = new File(filePath);
    return FileUtils.readFileToString(file, "UTF-8");
  }

  public void printRequestsCount() {

  }

  public int getMockedRequestCount() {
    return mockedRequestCount;
  }

  public int getNormalRequestCount() {
    return normalRequestCount;
  }

  public void loadBlazeMeterMocks() {
  }


  public void modifyMockedResponse(String url, String key, Object value) throws IOException {
    JSONObject mockedResponse = getMockedResponse(url);
    JSONObject resultValues = mockedResponse.getJSONArray("result")
        .getJSONObject(0);

    resultValues.put(key, value);
    saveResponse(mockedResponse, new URL(url));
  }

  public JSONObject getMockedResponse(String url) throws IOException {
    String responsePath = mockedResponses.get(url);
    String contentFromFilePath = getContentFromFilePath(responsePath);
    return JSONObject.fromObject(contentFromFilePath);
  }

  public Object getValueFromMockedResponse(String url, String key) throws IOException {
    JSONObject mockedResponse = getMockedResponse(url);
    JSONObject resultValues = mockedResponse.getJSONArray("result")
        .getJSONObject(0);
    return resultValues.get(key);
  }
}


