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

import com.bzm.api.exception.InterruptRuntimeException;
import com.bzm.api.explorer.base.BZAObject;
import com.bzm.api.utils.BlazeMeterUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;


import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Master is returned as a result of starting any test.
 * It has at least(or more) @link com.blazemeter.api.explorer.Session
 */
public class Master extends BZAObject {

    public Master(BlazeMeterUtils utils, String id, String name) {
        super(utils, id, name);
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    /**
     * POST request to 'https://a.blazemeter.com/api/v4/masters/{masterId}/public-token'
     *
     * @return public link to the report
     */
    public String getPublicReport() throws IOException, InterruptedException {
        logger.info("Get link to public report for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/public-token", encode(getId()));
        JSONObject request = new JSONObject();
        request.put("publicToken", "None");
        JSONObject response = utils.execute(utils.createPost(uri, request.toString()));

        // generated public url for functionalGui test
        if (testType != null && testType.equals(FUNCTIONAL_GUI_TEST)) {
            return String.format("%s/app/?public-token=%s#/accounts/-1/workspaces/-1/projects/-1/masters/%s/cross-browser-summary",
                    utils.getAddress(), extractPublicToken(response.getJSONObject("result")), getId());
        }
        // generated public url for test suite
        if(testType != null && testType.equals(TEST_SUITE))
        {
            return String.format("%s/app/?public-token=%s#/accounts/-1/workspaces/-1/projects/-1/masters/%s/suite-report",
                    utils.getAddress(), extractPublicToken(response.getJSONObject("result")), getId());
        }
        return String.format("%s/app/?public-token=%s#/masters/%s/summary", utils.getAddress(), extractPublicToken(response.getJSONObject("result")), getId());
    }

    public String getServerReport(String workspaceId, String testId) throws IOException {
        String uri = utils.getAddress() + "/api/v4/user";
        JSONObject response = utils.execute(utils.createGet(uri));
        String accountId = response.getJSONObject("result").getJSONObject("defaultProject").getString("accountId");

        uri = utils.getAddress() + String.format("/api/v4/tests/%s", testId);
        response = utils.execute(utils.createGet(uri));
        String projectsId = response.getJSONObject("result").getString("projectId");

        String reportUrl = String.format("/app/#/accounts/%s/workspaces/%s/projects/%s/masters/%s/cross-browser-summary", accountId, workspaceId, projectsId, getId());
        return utils.getAddress() + reportUrl;
    }

    public String getServerReportForTestSuite(String workspaceId, String testId) throws IOException
    {
        String uri = utils.getAddress() + "/api/v4/user";
        JSONObject response = utils.execute(utils.createGet(uri));
        String accountId = response.getJSONObject("result").getJSONObject("defaultProject").getString("accountId");

        uri = utils.getAddress() + String.format("/api/v4/multi-tests/%s", testId);
        response = utils.execute(utils.createGet(uri));
        String projectsId = response.getJSONObject("result").getString("projectId");

        String reportUrl = String.format("/app/#/accounts/%s/workspaces/%s/projects/%s/masters/%s/suite-report", accountId, workspaceId, projectsId, getId());
        return utils.getAddress() + reportUrl;
    }

    /**
     * GET request to 'https://a.blazemeter.com/api/v4/masters/{masterId}/reports/thresholds?format=junit'
     *
     * @return junit report as a string
     */
    public String getJUnitReport() throws IOException {
        logger.info("Get JUnit report for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/reports/thresholds?format=junit", encode(getId()));
        return utils.executeRequest(utils.createGet(uri));
    }

    /**
     * GET request to 'https://a.blazemeter.com/api/v4/sessions'
     *
     * @return list of Sessions Id
     */
    public List<Session> getSessions() throws IOException {
        logger.info("Get list of sessions for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/sessions?masterId=%s", encode(getId()));
        List<Session> sessions = new ArrayList<>();
        JSONObject response = utils.execute(utils.createGet(uri));
        JSONArray result = response.getJSONArray("result");
        for (int i = 0; i < result.size(); i++) {
            JSONObject so = result.getJSONObject(i);
            sessions.add(Session.fromJSON(utils, so));
        }
        return sessions;
    }

    /**
     * Post properties to master.
     * Step 1: Get list of sessions
     * Step 2: Post properties to each session
     */
    @Deprecated
    public void postProperties(String properties) throws IOException, InterruptedException {
        if (StringUtils.isBlank(properties)) {
            logger.warn("Properties are empty, won't be sent to master = " + getId());
            return;
        }
        logger.info("Post properties to master id=" + getId());
        try {
            List<Session> sessions = getSessions();
            postProperties(properties, sessions);
        } catch (InterruptedException | InterruptRuntimeException | InterruptedIOException ex) {
            logger.warn("Interrupt while post properties", ex);
            throw new InterruptedException("Interrupt while post properties");
        } catch (Exception ioe) {
            logger.error("Failed to get sessions for master id=" + getId(), ioe);
        }
    }

    protected void postProperties(String properties, List<Session> sessions) throws IOException, InterruptedException {
        JSONArray propertiesArray = Session.convertProperties(properties);
        for (Session session : sessions) {
            try {
                session.postProperties(propertiesArray);
            } catch (InterruptedException | InterruptRuntimeException | InterruptedIOException ex) {
                logger.warn("Interrupt while post properties to session", ex);
                throw new InterruptedException("Interrupt while post properties to session");
            } catch (Exception e) {
                logger.error("Failed to send properties for session id=" + session.getId(), e);
            }
        }
    }

    /**
     * Stop Master
     * POST request to 'https://a.blazemeter.com/api/v4/masters/{masterId}/stop'
     */
    public JSONArray stop() throws IOException {
        logger.info("Stop master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/stop", encode(getId()));
        HttpsURLConnection connection = utils.createPost(uri, "");
        JSONObject response = utils.execute(connection);
        return response.getJSONArray("result");
    }


    /**
     * Terminate Master
     * POST request to 'https://a.blazemeter.com/api/v4/masters/{masterId}/terminate'
     */
    public JSONArray terminate() throws IOException {
        logger.info("Terminate master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/terminate", encode(getId()));
        HttpsURLConnection connection = utils.createPost(uri, "");
        JSONObject response = utils.execute(connection);
        return response.getJSONArray("result");
    }


    /**
     * GET request to 'https://a.blazemeter.com/api/v4/masters/{masterId}/status?events=false'
     *
     * @return master status code
     */
    public int getStatus() throws IOException {
        logger.info("Get master status id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/status?events=false", encode(getId()));
        JSONObject result = utils.execute(utils.createGet(uri)).getJSONObject("result");
        return result.getInt("progress");
    }

    /**
     * GET request to 'https://a.blazemeter.com/api/v4/masters/{masterId}/reports/main/summary'
     *
     * @return summary as JSON object
     */
    public JSONObject getSummary() throws IOException {
        logger.info("Get summary for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/reports/main/summary", encode(getId()));
        JSONObject result = utils.execute(utils.createGet(uri)).getJSONObject("result");
        JSONArray summary = result.getJSONArray("summary");
        if (!summary.isEmpty()) {
            return extractSummary(summary.getJSONObject(0));
        }
        return new JSONObject();
    }

    /**
     * GET request to 'https://a.blazemeter.com/api/v4/masters/{masterId}'
     *
     * @return functional report as JSON object
     */
    public JSONObject getFunctionalReport() throws IOException {
        logger.info("Get functional report for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s", encode(getId()));
        JSONObject result = utils.execute(utils.createGet(uri)).getJSONObject("result");
        return result.has("functionalSummary") ? result.getJSONObject("functionalSummary") : new JSONObject();
    }

    /**
     * PATCH request to 'https://a.blazemeter.com/api/v4/masters/{masterId}'
     *
     * @return note which was applied to master if request was successful
     */
    public String postNotes(String note) throws IOException, InterruptedException {
        if (StringUtils.isBlank(note)) {
            logger.warn("Cannot send null or empty notes");
            return StringUtils.EMPTY;
        }
        logger.info("Post notes to master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s", encode(getId()));
        JSONObject requestBody = new JSONObject();
        requestBody.put("note", note);
        String data = requestBody.toString();
        HttpsURLConnection connection = utils.createPatch(uri, data);
        JSONObject response = utils.execute(connection);
        JSONObject result = response.getJSONObject("result");
        return result.getString("note");
    }


    /**
     * PATCH request to 'https://a.blazemeter.com/api/v4/masters/{masterId}'
     *
     * @return name which was applied to master if request was successful
     */
    public String setReportName(String reportName) throws IOException, InterruptedException {
        if (StringUtils.isBlank(reportName)) {
            logger.warn("Report name is null or empty");
            return StringUtils.EMPTY;
        }
        logger.info("Setting Report Name for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s", encode(getId()));
        JSONObject request = new JSONObject();
        request.put("name", reportName);
        JSONObject result = utils.execute(utils.createPatch(uri, request.toString())).getJSONObject("result");
        return result.getString("name");
    }

    /**
     * GET request to 'https://a.blazemeter.com/api/v4/masters/{masterId}/ci-status'
     *
     * @return ci-status as JSONObject
     */
    public JSONObject getCIStatus() throws IOException {
        logger.info("Get CI status for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/ci-status", encode(getId()));
        return utils.execute(utils.createGet(uri)).getJSONObject("result");
    }

    private String extractPublicToken(JSONObject result) {
        return result.getString("publicToken");
    }

    private JSONObject extractSummary(JSONObject sumserv) {
        JSONObject summary = new JSONObject();
        summary.put("avg", Math.round(sumserv.getDouble("avg") * 100.0) / 100.0);
        summary.put("min", sumserv.getInt("min"));
        summary.put("max", sumserv.getInt("max"));
        summary.put("tp90", sumserv.getInt("tp90"));
        summary.put("errorPercentage", Math.round((sumserv.getDouble("failed") / sumserv.getDouble("hits") * 100) * 100) / 100);
        int hits = sumserv.getInt("hits");
        double last = sumserv.getDouble("last");
        double first = sumserv.getDouble("first");
        summary.put("hits", sumserv.getInt("hits"));
        double diff = ((last - first) == 0) ? 1 : (last - first);
        summary.put("avgthrpt", Math.round(hits / diff * 100.0) / 100.0);
        return summary;
    }

    public static Master fromJSON(BlazeMeterUtils utils, JSONObject obj) {
        return new Master(utils, obj.getString("id"), obj.getString("name"));
    }

    public JSONObject getFunctionalCIStatus() throws IOException {
        logger.info("Get CI status for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s", encode(getId()));
        return utils.execute(utils.createGet(uri)).getJSONObject("result");
    }

    public JSONObject getPerformanceCIStatus() throws IOException {
        logger.info("Get CI status for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s/ci-status", encode(getId()));
        return utils.execute(utils.createGet(uri)).getJSONObject("result");
    }
    //  Calling API to get the No Data/Not Set result in hasData variable
    // hasData=true summary data is present, hasData=false summary data is NA
    public JSONObject getHasDataStatus() throws IOException
    {
        logger.info("Get CI status for master id=" + getId());
        String uri = utils.getAddress() + String.format("/api/v4/masters/%s", encode(getId()));
        return utils.execute(utils.createGet(uri)).getJSONObject("result");
    }
}