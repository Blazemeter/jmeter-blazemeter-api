package com.bzm;

import com.bzm.api.explorer.CorrelationAsset;
import com.bzm.api.explorer.Account;
import com.bzm.api.explorer.User;
import com.bzm.api.explorer.Workspace;
import com.bzm.api.utils.BlazeMeterUtils;
import com.bzm.api.utils.JsonUtils;
import com.bzm.api.utils.MockedBlazeMeterUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlazeMeterManager {
  private static BlazeMeterManager instance;
  private MockedBlazeMeterUtils blazeMeterUtils;
  private String apiId;
  private String apiSecret;
  private String apiFilePath;
  private User currentUser;
  private Account currentUserDefaultAccount;

  private BlazeMeterManager() {
    // Private constructor to prevent direct instantiation
  }

  public static synchronized BlazeMeterManager getInstance() {
    if (instance == null) {
      instance = new BlazeMeterManager();
    }
    return instance;
  }

  public void initialize(String apiFilePath) throws IOException {
    this.apiFilePath = apiFilePath;
    JSONObject apiCredentials = JsonUtils.getJsonFromFilepath(apiFilePath);
    apiId = apiCredentials.getString("id");
    apiSecret = apiCredentials.getString("secret");
    blazeMeterUtils = new MockedBlazeMeterUtils(apiId, apiSecret);
  }


  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  public void setApiSecret(String apiSecret) {
    this.apiSecret = apiSecret;
  }

  /**
   * Returns true if the manager has been initialized with credentials. Note that
   * this does not mean that the credentials are valid.
   * @return true if the manager has been initialized with credentials, false otherwise
   */
  public boolean isConfigured() {
    return isNotEmpty(apiId) && isNotEmpty(apiSecret);
  }

  private boolean isNotEmpty(String str) {
    return str != null && !str.isEmpty();
  }

  /**
   * Returns true if the credentials are valid, false otherwise
   * @return true if the credentials are valid, false otherwise
   */
  public boolean isValid() {
    User user = getUser();
    if (user == null) {
      return false;
    }

    // If the user is not null, we refresh the current user
    this.currentUser = user;
    return true;
  }

  /**
   * Returns true if the credentials has permissions to access the specified workspace, false otherwise
   * @param workspaceId the workspace id
   *
   * @return true if the credentials has permissions to access the specified workspace, false otherwise
   */
  public boolean hasAccessToWorkspace(String workspaceId) {
    try {
      List<Workspace> workspaces = getWorkspaces(getAccounts(getUser()).get(0));
      for (Workspace workspace : workspaces) {
        if (workspace.getId().equals(workspaceId)) {
          return true;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public void initialize(String apiId, String apiSecret) {
    blazeMeterUtils = new MockedBlazeMeterUtils(apiId, apiSecret);
  }

  /**
   * Returns the Ids of the workspaces associated with the default account
   * @return
   */
  public List<Long> getWorkspacesIds() {
    List<Long> workspacesIds = new ArrayList<>();
    try {
      updateCurrentUser();
      updateCurrentUserDefaultAccount();

      for (Workspace workspace : getWorkspaces(this.currentUserDefaultAccount)) {
        workspacesIds.add(Long.parseLong(workspace.getId()));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return workspacesIds;
  }

  private void updateCurrentUserDefaultAccount() {
    if (this.currentUserDefaultAccount == null) {
      this.currentUserDefaultAccount = currentUser.getDefaultAccount();
    }
  }

  private void updateCurrentUser() {
    if (this.currentUser == null) {
      this.currentUser = getUser();
    }
  }

  /**
   * Returns the workspaces associated with defaultAccount
   * @return list of  workspaces
   */
  public List<Workspace> getWorkspacesForDefaultUserAccount() {
    try {
      updateCurrentUser();
      updateCurrentUserDefaultAccount();
      return getWorkspaces(this.currentUserDefaultAccount);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public String getDefaultWorkspaceId() {
    updateCurrentUser();
    if (this.currentUser != null) {
      return this.currentUser.getDefaultWorkspaceId();
    } else {
      return null;
    }
  }

  public User getUser() {
    if (!isConfigured()) {
      return null;
    }

    try {
      this.currentUser = User.getUser(blazeMeterUtils);
      return this.currentUser;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public List<CorrelationAsset> getSystemCatalogWithoutData(String workspaceId) throws IOException {
    if (!isConfiguredAndValid()) {
      return new ArrayList<>();
    }

    return new CorrelationAsset(blazeMeterUtils, workspaceId).getAssets()
        .withSystem()
        .retrieveAll();
  }

  public List<Long> getSharedWorkspacesIds() {
    List<Long> sharedWorkspacesIds = new ArrayList<>();
    for (Workspace workspace : getSharedWorkspaces()) {
      sharedWorkspacesIds.add(Long.parseLong(workspace.getId()));
    }
    return sharedWorkspacesIds;
  }

  public List<Workspace> getSharedWorkspaces() {
    List<Account> sharedAccounts = getUser().getSharedAccounts();
    List<Workspace> sharedWorkspaces = new ArrayList<>();
    for (Account account : sharedAccounts) {
      try {
        sharedWorkspaces.addAll(getWorkspaces(account));
      } catch (IOException e) {
        blazeMeterUtils.getLogger()
            .error("Error while retrieving shared workspaces for account " + account.getId());
      }
    }
    return sharedWorkspaces;
  }

  public boolean isConfiguredAndValid() {
    if (!isConfigured()) {
      blazeMeterUtils.getLogger().error("API Keys are not configured");
      return false;
    }

    if (!isValid()) {
      blazeMeterUtils.getLogger().error("API Keys are not valid or they expired. Please check your API Keys");
      return false;
    }

    return true;
  }

  public List<CorrelationAsset> getBlazemeterCatalogWithData(String workspaceId) throws IOException {
    if (!isConfiguredAndValid()) {
      return new ArrayList<>();
    }

    return new CorrelationAsset(blazeMeterUtils, workspaceId).getAssets()
        .withSystem()
        .withData()
        .retrieveAll();
  }

  public List<CorrelationAsset> getWorkspaceCatalogWithData(String workspaceId) throws IOException {
    if (!isConfiguredAndValid()) {
      return new ArrayList<>();
    }

    return new CorrelationAsset(blazeMeterUtils, workspaceId).getAssets()
        .withCustomDefined()
        .withData()
        .retrieveAll();
  }

  public List<CorrelationAsset> getBlazeMeterVersions(String workspaceId, List<Pair<String, String>> protocolVersions)
      throws IOException {
    if (!isConfiguredAndValid()) {
      return new ArrayList<>();
    }

    CorrelationAsset.CorrelationAssetBuilder requestBuilder =
        new CorrelationAsset(blazeMeterUtils, workspaceId).getAssets()
            .withData()
            .withSystem()
            .withoutShared();

    return getTemplateVersions(requestBuilder, protocolVersions);
  }

  private List<CorrelationAsset> getTemplateVersions(CorrelationAsset.CorrelationAssetBuilder requestBuilder,
                                                     List<Pair<String, String>> protocolVersions) throws IOException {
    if (!isConfiguredAndValid()) {
      return new ArrayList<>();
    }

    List<CorrelationAsset> correlationAssets = new ArrayList<>();
    for (Pair<String, String> protocolVersion : protocolVersions) {
      List<CorrelationAsset> assets = getCorrelationAssets(requestBuilder
          .forProtocol(protocolVersion.getLeft())
          .withVersion(protocolVersion.getRight()));
      correlationAssets.addAll(assets);
    }
    return correlationAssets;
  }

  public List<CorrelationAsset> getWorkspaceVersions(String workspaceId, List<Pair<String, String>> protocolVersions)
      throws IOException {

    CorrelationAsset.CorrelationAssetBuilder requestBuilder =
        new CorrelationAsset(blazeMeterUtils, workspaceId).getAssets()
            .withData()
            .withCustomDefined();

    return getTemplateVersions(requestBuilder, protocolVersions);
  }

  public BlazeMeterUtils getBlazeMeterUtils() {
    return blazeMeterUtils;
  }

  public MockedBlazeMeterUtils getMockedBlazeMeterUtils() {
    return blazeMeterUtils;
  }

  public String getApiId() {
    return apiId;
  }

  public String getApiSecret() {
    return apiSecret;
  }

  public List<Account> getAccounts() throws IOException {
    return getUser().getAccounts();
  }

  public List<Account> getAccounts(User user) throws IOException {
    return user.getAccounts();
  }

  public Account getDefaultAccount() throws IOException {
    return getUser().getDefaultAccount();
  }

  /**
   * Get workspaces for the provided account
   * @param account
   * @return
   * @throws IOException
   */
  public List<Workspace> getWorkspaces(Account account) throws IOException {
    return account.getWorkspaces();
  }

  public Workspace getDefaultWorkspace(Account account) throws IOException {
    return account.getWorkspaces().stream()
        .findFirst().orElseThrow(() ->
            new RuntimeException("No workspaces found for account "
                + account.getName() + " with id " + account.getId() + "."));
  }

  public Workspace getDefaultWorkspace() throws IOException {
    return getDefaultWorkspace(getDefaultAccount());
  }

  public Workspace getWorkspace(String workspaceId) throws IOException {
    return Workspace.getWorkspace(blazeMeterUtils, workspaceId);
  }

  public List<CorrelationAsset> getCorrelationAssets(Workspace workspace) throws IOException {
    CorrelationAsset asset = new CorrelationAsset(blazeMeterUtils, workspace.getId());
    return asset.getCorrelationAssets(workspace.getId(), false, false);
  }

  public List<CorrelationAsset> getCorrelationAssets(CorrelationAsset.CorrelationAssetBuilder builder) throws IOException {
    return builder.retrieveAll();
  }

  public void loadMockedResponses() {
    blazeMeterUtils.loadBlazeMeterMocks();
  }

  public void printRequestsCount() {
    blazeMeterUtils.printRequestsCount();
  }

  public void clearMockedResponses() {
    blazeMeterUtils.clearMockedResponses();
  }
}



