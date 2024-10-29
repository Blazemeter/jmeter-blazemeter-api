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

package com.bzm.api.explorer;

import com.bzm.api.explorer.base.BZAObject;
import com.bzm.api.utils.BlazeMeterUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Describes user. User may have one or more accounts.
 */
public class User extends BZAObject {

    private String defaultAccountId;
    private String defaultAccountName;
    private String defaultWorkspaceName;
    private String defaultWorkspaceId;

    public User(BlazeMeterUtils utils) {
        super(utils, "", "");
    }

    public User(BlazeMeterUtils utils, String id, String name) {
        super(utils, id, name);
    }

    /**
     * GET request to 'https://a.blazemeter.com/api/v4/user'
     * @return User for current credentials
     */
    public static User getUser(BlazeMeterUtils utils) throws IOException {
        String uri = utils.getAddress() + "/api/v4/user";
        HttpsURLConnection get = utils.createGet(uri);
        JSONObject response = utils.execute(get);
        JSONObject result = response.getJSONObject("result");
        return User.fromJSON(utils, result);
    }

    public String getDefaultAccountId() {
        return defaultAccountId;
    }

    public String getDefaultAccountName() {
        return defaultAccountName;
    }

    public String getDefaultWorkspaceName() {
        return defaultWorkspaceName;
    }

    public String getDefaultWorkspaceId() {
        return defaultWorkspaceId;
    }

    /**
     * Get Account
     * limit = 1000, sorted by name
     */
    public List<Account> getAccounts() throws IOException {
        return getAccounts("1000", "name");
    }

    /**
     * Get Accounts
     * GET request to 'https://a.blazemeter.com/api/v4/accounts'
     * @param limit of tests count in returned list
     * @param sort sort type: 'name', 'updated' or other * @return list of Account for user token
     */
    public List<Account> getAccounts(String limit, String sort) throws IOException {
        logger.info("Get list of accounts");
        String uri = utils.getAddress()+ "/api/v4/accounts";
        uri = addParamToUrl(uri, "sort%5B%5D", sort); // 'sort%5B%5D' == 'sort[]'
        uri = addParamToUrl(uri, "limit", limit);
        JSONObject response = utils.execute(utils.createGet(uri));
        return extractAccounts(response.getJSONArray("result"));
    }

    private List<Account> extractAccounts(JSONArray result) {
        List<Account> accounts = new ArrayList<>();

        for (Object obj : result) {
            accounts.add(Account.fromJSON(utils, (JSONObject) obj));
        }

        return accounts;
    }

    public List<Account> getSharedAccounts()  {
        List<Account> accounts = new ArrayList<>();
        try {
            accounts = getAccounts();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return accounts.stream()
            .filter(account -> !this.defaultAccountId.equals(account.getId()))
            .collect(Collectors.toList());
    }

    public static User fromJSON(BlazeMeterUtils utils, JSONObject result) {
        User user = new User(utils, result.getString("id"), result.getString("email"));
        // From result we will get defaultAccountId, defaultAccountName, defaultWorkspaceName, defaultWorkspaceId
        JSONObject defaultProject = result.getJSONObject("defaultProject");

        user.setDefaultAccountId(defaultProject.getString("accountId"));
        user.setDefaultAccountName(defaultProject.getString("accountName"));
        user.setDefaultWorkspaceName(defaultProject.getString("workspaceName"));
        user.setDefaultWorkspaceId(defaultProject.getString("workspaceId"));
        return user;
    }

    public void setDefaultAccountId(String defaultAccountId) {
        this.defaultAccountId = defaultAccountId;
    }

    public void setDefaultAccountName(String defaultAccountName) {
        this.defaultAccountName = defaultAccountName;
    }

    public void setDefaultWorkspaceName(String defaultWorkspaceName) {
        this.defaultWorkspaceName = defaultWorkspaceName;
    }

    public void setDefaultWorkspaceId(String defaultWorkspaceId) {
        this.defaultWorkspaceId = defaultWorkspaceId;
    }

    @Override
    public String toString() {
        return "{" +
            "\"id\"=\"" + id + "\"," +
            "\"name\"=\"" + name + "\"}";
    }

    public Account getDefaultAccount() {
        return new Account(utils, defaultAccountId, defaultAccountName);
    }
}
