package com.bzm.api.explorer;

import com.bzm.api.ApiResponse;
import com.bzm.api.explorer.base.BZAObject;
import com.bzm.api.utils.BlazeMeterUtils;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * Correlation Asset holds the information of the Asset from the API.
 */
public class CorrelationAsset extends BZAObject {

  private String packageId;
  private String id;
  private String type;
  private String version;
  private String protocol;
  private String description;
  private String workspaceId;
  private File correlationRulesFile;
  private String correlationRulesFileAsString;
  private boolean shouldGetFile;
  private boolean isSystem = false;
  private boolean isAccShared = false;
  private boolean isEnterprise = false;
  private boolean isDataAccesible = false;
  private String author;

  private String fileName;
  private String displayName;

  public CorrelationAsset(BlazeMeterUtils utils) {
    super(utils, "", "");
  }

  public CorrelationAsset(BlazeMeterUtils utils, String workspaceId) {
    super(utils, "", "");
    this.workspaceId = workspaceId;
  }

  public static List<String> getUploadedFilesNames(BlazeMeterUtils blazeMeterUtils,
                                                   String workspaceId) {
    String uri = blazeMeterUtils.getAddress() + "/api/v4/workspaces/" + workspaceId + "/assets";
    JSONObject response = null;
    try {
      response = blazeMeterUtils.execute(blazeMeterUtils.createGet(uri));
    } catch (IOException e) {
      e.printStackTrace();
    }
    JSONArray assets = response.getJSONArray("result");
    // parse assets to a list of names
    List<String> names = new ArrayList<>();
    for (Object asset : assets) {
      names.add(((JSONObject) asset).getString("name"));
    }
    return names;
  }

  public File getFile(String assetId) throws IOException {
    String uri = utils.getAssetUri(workspaceId, assetId);
    Object response = this.utils.executeRequest(utils.createGet(uri));
    if (response instanceof String) {
      File file = File.createTempFile("assetFile", ".json", new File(System.getProperty("java.io.tmpdir")));

      FileWriter writer = new FileWriter(file);
      writer.write(response.toString());
      writer.close();
      return file;
    }

    return null;
  }

  /**
   * @deprecated use the CorrelationAssetBuilder with withFile() instead
   * We are keeping this method just in case
   */
  @Deprecated
  public String getFileAsString(String assetId) throws IOException {
    String uri = utils.getAssetUri(workspaceId, assetId);
    Object response = this.utils.executeRequest(utils.createGet(uri));
    if (response != null) {
      return response.toString();
    }

    return null;
  }

  private List<CorrelationAsset> findAll(boolean withSystem) throws IOException {
    String uri = utils.getAssetsAddress(workspaceId);
    uri = addParamToUrl(uri, "withSystem", withSystem ? "true" : "false");
    uri = addParamToUrl(uri, "withAccShared", "true");
    uri = addParamToUrl(uri, "limit", "100");
    uri = addParamToUrl(uri, "skip", "0");
    uri = addParamToUrl(uri, "q", "type=correlation-rule");
    JSONObject response = utils.execute(utils.createGet(uri));

    return extractAll(response.getJSONArray("result"));
  }

  private List<CorrelationAsset> extractAll(JSONArray result) {
    return (List<CorrelationAsset>) result.stream()
        .map(asset -> fromJSON(utils, (JSONObject) asset))
        .collect(Collectors.toList());
  }

  public static CorrelationAsset fromJSON(BlazeMeterUtils utils, JSONObject obj) {
    CorrelationAsset asset = new CorrelationAsset(utils);
    asset.setId(obj.getString("id"));
    asset.setType(obj.getString("type"));
    asset.setWorkspaceId(obj.getString("workspaceId"));
    asset.setPackageId(obj.getString("packageId"));

    if (obj.get("isSystem") instanceof Boolean) {
      asset.setSystem(obj.getBoolean("isSystem"));
      asset.setAuthor("BlazeMeter");
    }

    if (obj.get("isAccShared") instanceof Boolean) {
      asset.setIsAccShared(obj.getBoolean("isAccShared"));
    }

    if (obj.get("isEnterprise") instanceof Boolean) {
      asset.setIsEnterprise(obj.getBoolean("isEnterprise"));
    }

    if (obj.get("dataAccessible") instanceof Boolean) {
      boolean isDataAccesible = obj.getBoolean("dataAccessible");
      asset.setDataAccesible(isDataAccesible);
    }

    if (obj.containsKey("data")) {
      JSONObject data = (JSONObject) obj.get("data");
      asset.setCorrelationRulesFileAsString(data.getString("content"));
    }

    JSONObject metadata = (JSONObject) obj.get("metadata");
    asset.setVersion(metadata.getString("version"));
    String protocolName = metadata.getString("application");
    asset.setProtocol(protocolName);
    asset.setDescription(asset.getString(metadata, "description"));
    asset.setName(protocolName);

    return asset;
  }

  private void setAuthor(String author) {
    this.author = author;
  }


  private void setIsEnterprise(boolean isEnterprise) {
    this.isEnterprise = isEnterprise;
  }

  public boolean isEnterprise() {
    return isEnterprise;
  }

  /**
   * If the user has permissions to see the content of the asset. This is usually
   * determined by the user's payment plan. Free users can't see the content of
   * the asset.
   * @return true if the user has permissions to see the content of the asset.
   */
  public boolean isDataAccesible() {
    return isDataAccesible;
  }

  public void setDataAccesible(boolean dataAccesible) {
    isDataAccesible = dataAccesible;
  }

  private String getString(JSONObject obj, String key) {
    if (obj.containsKey(key)) {
      return obj.getString(key);
    } else {
      return "empty";
    }
  }

  /**
   * Creates both the Correlation Asset registry and the Correlation Asset file
   * @return the created Correlation Asset
   * @throws IOException
   */
  public CorrelationAsset create(String fileName, String content) throws IOException {
    JSONObject body = new JSONObject();
    body.put("name", getName() + "_" + getVersion().replace(".", "_"));
    body.put("workspaceId", workspaceId);
    body.put("type", "correlation-rule");
    body.put("isSystem", false);
    body.put("isEnterprise", false);
    body.put("isAccShared", true);
    body.put("isDigital", false);

    JSONObject metadata = new JSONObject();
    metadata.put("version", getVersion());
    metadata.put("application", getName());
    metadata.put("description", getDescription());
    body.put("metadata", metadata);

    JSONObject data = new JSONObject();
    data.put("content", content);
    data.put("fileName", fileName);
    body.put("data", data);

    HttpsURLConnection createAssetRegistryConnection = utils
        .createPost(utils.getAssetsAddress(workspaceId), body.toString());
    ApiResponse response = makePost(createAssetRegistryConnection, body.toString());
    if (response.hasErrors()) {
      throw new IOException("Error creating asset: " + response.getErrorMessage());
    }

    return fromJSON(utils, response.getResult());
  }

  private ApiResponse makePost(HttpsURLConnection connection, String body) throws IOException {
    // Send the request
    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
    outputStream.write(body.getBytes(StandardCharsets.UTF_8));
    outputStream.flush();
    outputStream.close();

    // Get the response
    int responseCode = connection.getResponseCode();
    BufferedReader reader;
    if (responseCode >= 200 && responseCode < 300) {
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    } else {
      reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
    }

    String line;
    StringBuilder response = new StringBuilder();
    while ((line = reader.readLine()) != null) {
      response.append(line);
    }
    reader.close();

    return new ApiResponse(response.toString());
  }

  public List<CorrelationAsset> getCorrelationAssets(String workspaceId, boolean withFile, boolean withSystem) throws IOException {
    if (workspaceId == null) {
      throw new IllegalArgumentException("workspaceId is null");
    }

    setWorkspaceId(workspaceId);
    Workspace workspace = getWorkspace(workspaceId);
    List<CorrelationAsset> assetsByWorkspace = findAll(withSystem);
    if (!withFile) {
      return assetsByWorkspace;
    }

    List<CorrelationAsset> assets = new java.util.ArrayList<>();
    for (CorrelationAsset asset : assetsByWorkspace) {
      asset.setCorrelationRulesFileAsString(getFileAsString(asset.getId()));
      assets.add(asset);
    }
    return assets;
  }

  public Workspace getWorkspace(String workspaceId) throws IOException {
    return Workspace.getWorkspace(utils, workspaceId);
  }

  protected JSONObject processResponse(String response) {
    return JSONObject.fromObject(response);
  }

  public void setWorkspaceId(String workspaceId) {
    this.workspaceId = workspaceId;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getVersion() {
    return version;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPackageId() {
    return packageId;
  }

  public void setPackageId(String packageId) {
    this.packageId = packageId;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public String getProtocol() {
    return protocol;
  }

  public String getDescription() {
    return description;
  }

  public String getWorkspaceId() {
    return workspaceId;
  }

  public boolean isAccShared() {
    return isAccShared;
  }

  private void setSystem(boolean isSystem) {
    this.isSystem = isSystem;
  }

  public void setCorrelationRulesFile(File file) {
    this.correlationRulesFile = file;
  }

  public void setCorrelationRulesFileAsString(String correlationRulesFileAsString) {
    this.correlationRulesFileAsString = correlationRulesFileAsString;
  }

  /**
   * Returns if the current asset can be used for analysis or not
   * @return
   */
  public boolean canUse() {
    return isDataAccesible;
  }

  @Override
  public boolean equals(Object obj) {
    CorrelationAsset asset = (CorrelationAsset) obj;
    File correlationFile = asset.getCorrelationFile();
    return asset.getName().equals(name)
        && asset.getType().equals(type)
        && asset.getProtocol().equals(protocol)
        && asset.getVersion().equals(version)
        && asset.getDescription().equals(description)
        && asset.getId().equals(id)
        && ((correlationFile == null && correlationFile == null)
        || (correlationFile != null && correlationFile.equals(correlationRulesFile)))
        && asset.isSystem() == isSystem
        && asset.isAccShared() == isAccShared;
  }


  public void shouldGetFile(boolean shouldGetFile) {
    this.shouldGetFile = shouldGetFile;
  }

  public File getCorrelationFile() {
    return correlationRulesFile;
  }


  public String getCorrelationRulesFileAsString() {
    return correlationRulesFileAsString;
  }

  public boolean isSystem() {
    return this.isSystem;
  }

  public void setIsAccShared(boolean isAccShared) {
    this.isAccShared = isAccShared;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String toJSON() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("id", id);
    jsonObject.put("type", type);
    jsonObject.put("version", version);
    jsonObject.put("protocol", protocol);
    jsonObject.put("description", description);
    jsonObject.put("workspaceId", workspaceId);
    jsonObject.put("shouldGetFile", shouldGetFile);
    jsonObject.put("isSystem", isSystem);
    jsonObject.put("isAccShared", isAccShared);
    jsonObject.put("isDataAccesible", isDataAccesible);

    return jsonObject.toString();
  }

  /**
   * Since the possibilities of the API are very wide in terms of filtering, we are
   * creating this GetAssets Builder to make it easier to use the API.
   * We want to be able to do something like this:
   *       List<CorrelationAsset> assets = BlazeMeterManager.getInstance().GetAssets()
   *           .fromWorkspace(String.valueOf(workspaceId))
   *           .withSystem()
   *           .withoutFile()
   *           .withoutShared()
   *           .retrieve();
   *
   * Meaning: Get me the Correlation Assets from the workspace with id workspaceId,
   * that comes from system, without the Correlation File, without the shared assets.
   */

  public CorrelationAssetBuilder getAssets() {
    return new CorrelationAssetBuilder();
  }

  public class CorrelationAssetBuilder {
    private boolean defaultWorkspace;
    private boolean withSystem;
    private boolean withData;
    private boolean withShared;
    private String forProtocol;
    private String withVersion;

    private String assetId;
    private boolean withEnterprise;
    private String withPackageId;
    private boolean withCustomDefined;


    public CorrelationAssetBuilder withPackageId(String withPackageId) {
      this.withPackageId = withPackageId;
      return this;
    }


    public CorrelationAssetBuilder withSystem() {
      this.withSystem = true;
      return this;
    }

    public CorrelationAssetBuilder withEnterprise() {
      this.withEnterprise = true;
      return this;
    }

    public CorrelationAssetBuilder withoutSystem() {
      this.withSystem = false;
      return this;
    }

    public CorrelationAssetBuilder withData() {
      this.withData = true;
      return this;
    }

    public CorrelationAssetBuilder withoutData() {
      this.withData = false;
      return this;
    }

    public CorrelationAssetBuilder withShared() {
      this.withShared = true;
      return this;
    }

    public CorrelationAssetBuilder withoutShared() {
      this.withShared = false;
      return this;
    }

    public CorrelationAssetBuilder withCustomDefined() {
      this.withCustomDefined = true;
      return this;
    }

    public CorrelationAssetBuilder withAssetId(String assetId) {
      this.assetId = assetId;
      return this;
    }

    public CorrelationAssetBuilder forProtocol(String protocol) {
      this.forProtocol = protocol;
      return this;
    }

    public CorrelationAssetBuilder withVersion(String version) {
      this.withVersion = version;
      return this;
    }

    public CorrelationAsset retrieve() throws IOException {
      String uri = prepareBaseUri();
      uri = addParamToUrl(uri, "q", "metadata.application=" + this.forProtocol);
      uri = addParamToUrl(uri, "q", "metadata.version=" + this.withVersion);

      JSONObject response = utils.execute(utils.createGet(uri));
      CorrelationAsset correlationAsset = fromJSON(utils, response.getJSONObject("result"));
      if (withData) {
        correlationAsset.setCorrelationRulesFileAsString(getFileAsString(correlationAsset.getId()));
      }
      return correlationAsset;
    }

    private String prepareBaseUri() {
      String uri = (this.assetId == null) ? utils.getAssetsAddress(workspaceId)
          : utils.getAssetAddress(workspaceId, this.assetId);
      String withAccShared = System.getProperty("blazemeter.api.syncAccountShared", "false");
      uri = addParamToUrl(uri, "withAccShared", withAccShared);
      uri = addParamToUrl(uri, "withSystem", this.withSystem);
      uri = addParamToUrl(uri, "withData", this.withData);
      uri = addParamToUrl(uri, "limit", "100");
      uri = addParamToUrl(uri, "skip", "0");
      uri = addParamToUrl(uri, "q", "type=correlation-rule");
      return uri;
    }

    public List<CorrelationAsset> retrieveAll() throws IOException {
      String uri = prepareBaseUri();
      if (this.forProtocol != null) {
        uri = addParamToUrl(uri, "q", "metadata.application=" + this.forProtocol);
      }

      if (this.withVersion != null) {
        uri = addParamToUrl(uri, "q", "metadata.version=" + this.withVersion);
      }

      if (this.withPackageId != null) {
        uri = addParamToUrl(uri, "q", "packageId=" + this.withPackageId);
      }

      HttpsURLConnection getRequest = utils.createGet(uri);
      JSONObject response = utils.execute(getRequest);
      if (response == null) {
        return new ArrayList<>();
      }

      List<CorrelationAsset> assets = extractAll(response.getJSONArray("result"));
      List<CorrelationAsset> filteredAssets = new ArrayList<>();
      //We do the filtering manually because their API has a bug that also retrieves assets that accShared when we dont want them
      for (CorrelationAsset asset : assets) {
        if (shouldFilterSystem(asset)) {
          continue;
        }

        if (shouldFilterUserDefined(asset)) {
          continue;
        }

        filteredAssets.add(asset);
      }
      return filteredAssets;
    }

    private boolean shouldFilterSystem(CorrelationAsset asset) {
      return !this.withSystem && asset.isSystem();
    }

    private boolean shouldFilterEnterprise(CorrelationAsset asset) {
      return !this.withEnterprise && asset.isEnterprise;
    }

    private boolean shouldFilterUserDefined(CorrelationAsset asset) {
      return !this.withCustomDefined && !asset.isSystem();
    }
  }

  public String getAuthor() {
    return author;
  }

  @Override
  public String toString() {
    return "{" +
        "\"WS\":\"" + workspaceId + "\"," +
        "\"isSystem\":" + prettyBoolean(isSystem) + "," +
        "\"isAccShared\":" + prettyBoolean(isAccShared) + "," +
        "\"isEnterprise\":" + prettyBoolean(isEnterprise) + "," +
        "\"isDataAccesible\":" + prettyBoolean(isDataAccesible) + "," +
        "\"P\":\"" + protocol + "\"," +
        "\"V\":\"" + version + "\"," +
        "\"name\":\"" + name + "\"" +
        "\"description\":\"" + description + "\"," +
        "\"correlationRulesFile\":" + correlationRulesFile + "," +
        "\"correlationRulesFileAsString\":\"" + correlationRulesFileAsString + "\"," +
        "\"shouldGetFile\":" + shouldGetFile + "," +
        "\"id\":\"" + id + "\"," +
        "\"type\":\"" + type + "\"," +
        "}";
  }

  private String prettyBoolean(boolean bool) {
    return bool ? "true " : "false";
  }
}

