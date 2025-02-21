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

package com.bzm.api.explorer.test;

import com.bzm.api.explorer.Master;
import com.bzm.api.explorer.Session;
import com.bzm.api.explorer.base.BZAObject;
import com.bzm.api.utils.BlazeMeterUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Corresponds to '.jmeter' or '.http' or '.followme' or '.http' or '.taurus' tests on server.
 */
public class SingleTest extends AbstractTest {

    private static String TESTS = "/api/v4/tests";

    public SingleTest(BlazeMeterUtils utils, String id, String name, String testType) {
        super(utils, id, name, testType);
    }

    /**
     * POST request to 'https://a.blazemeter.com/api/v4/tests/{testId}/start'
     */
    @Override
    public Master start() throws IOException {
        logger.info("Start single test id=" + getId());
        JSONObject result = sendStartTest(utils.getAddress() + String.format(TESTS + "/%s/start", encode(getId())));
        fillFields(result);
        return master;
    }


    /**
     * POST request to 'https://a.blazemeter.com/api/v4/tests/{testId}/start'
     */
    @Override
    public Master startWithProperties(String properties) throws IOException {
        logger.info("Start single test id=" + getId());
        JSONObject result = sendStartTestWithBody(utils.getAddress() + String.format(TESTS + "/%s/start", encode(getId())), prepareSessionProperties(properties));
        fillFields(result);
        return master;
    }

    /**
     * POST request to 'https://a.blazemeter.com/api/v4/tests/{testId}/start-external'
     */
    @Override
    public Master startExternal() throws IOException {
        logger.info("Start external single test id=" + getId());
        JSONObject result = sendStartTest(utils.getAddress() + String.format(TESTS + "/%s/start-external", encode(getId())));
        fillFields(result);
        return master;
    }

    @Override
    public void uploadFile(File file) throws IOException {
        logger.info("Upload file to single test id=" + getId());
        String uri = utils.getAddress() + String.format(TESTS + "/%s/files", encode(getId()));
        HttpsURLConnection connection = utils.createPost(uri, file);
        JSONObject response = utils.execute(connection);
        logger.info("File uploaded with response: " + response);
    }


    @Override
    public void update(String data) throws IOException {
        logger.info(String.format("Update single test id=%s data=%s", getId(), data));
        String uri = utils.getAddress() + String.format(TESTS + "/%s", encode(getId()));
        JSONObject object = utils.execute(utils.createPatch(uri, data));
        logger.info("Single test was updated with response: " + object);
    }

    /**
     * Prepare JSON for PATCH BlazeMeter Test
     * @param filename - file name without absolute path
     */
    public void updateTestFilename(String filename) throws  IOException {
        logger.info(String.format("Update single test id=%s filename=%s", getId(), filename));

        if ("jmeter".equals(testType)) {
            updateJMeterTestFilename(filename);
        } else if ("taurus".equals(testType) || "functionalApi".equals(testType)) {
            updateTaurusTestFilename(filename);
        } else {
            logger.warn(String.format("This test type '%s' does not support script configuration", testType));
        }
    }

    protected void updateTaurusTestFilename(String filename) throws IOException {
        JSONObject configuration = new JSONObject();
        configuration.put("testMode", "script");
        configuration.put("filename", filename);

        if (filename.endsWith(".jmx")) {
            configuration.put("scriptType", "jmeter");
        } else if (filename.endsWith(".yml") || filename.endsWith(".yaml")) {
            configuration.put("scriptType", "taurus");
        } else {
            logger.warn("Unknown script type. Please, select 'Test type' in BlazeMeter web application");
        }

        JSONObject data = new JSONObject();
        data.put("configuration", configuration);
        update(data.toString());
    }

    protected void updateJMeterTestFilename(String filename) throws IOException {
        JSONObject jmeter = new JSONObject();
        jmeter.put("filename", filename);

        JSONObject plugins = new JSONObject();
        plugins.put("jmeter", jmeter);

        JSONObject configuration = new JSONObject();
        configuration.put("plugins", plugins);

        JSONObject data = new JSONObject();
        data.put("configuration", configuration);
        update(data.toString());
    }

    /**
     * Get single test
     * GET request to 'https://a.blazemeter.com/api/v4/tests/{testId}'
     *
     * @param utils - BlazeMeterUtils that contains logging and http setup
     * @param id    - test Id
     * @return SingleTest entity, which contains test ID and name (test label)
     */
    public static SingleTest getSingleTest(BlazeMeterUtils utils, String id) throws IOException {
        Logger utilsLogger = utils.getLogger();
        utilsLogger.info("Get Single Test id=" + id);
        String uri = utils.getAddress() + String.format(TESTS + "/%s", BZAObject.encode(utilsLogger, id));
        JSONObject response = utils.execute(utils.createGet(uri));
        return SingleTest.fromJSON(utils, response.getJSONObject("result"));
    }

    public void validateFiles(List<String> fileNames) throws IOException {
        logger.info(String.format("Validate files in single test id=%s files=%s", getId(), Arrays.toString(fileNames.toArray(new String[0]))));
        JSONArray files = new JSONArray();

        for (String fileName : fileNames) {
            JSONObject obj = new JSONObject();
            obj.put("fileName", fileName);
            files.add(obj);
        }

        JSONObject data = new JSONObject();
        data.put("files", files);

        validate(data.toString());
    }

    @Override
    public void validate(String data) throws IOException {
        logger.info(String.format("Validate single test id=%s data=%s", getId(), data));
        String uri = utils.getAddress() + String.format(TESTS + "/%s/validate", encode(getId()));
        JSONObject object = utils.execute(utils.createPost(uri, data));
        logger.info("Request for validate single test got response: " + object);
    }

    @Override
    public JSONArray validations() throws IOException {
        logger.info("Get validations for single test id=" + getId());
        String uri = utils.getAddress() + String.format(TESTS + "/%s/validations", encode(getId()));
        JSONObject object = utils.execute(utils.createGet(uri));
        return object.getJSONArray("result");
    }

    @Override
    public void fillFields(JSONObject result) {
        this.signature = Session.UNDEFINED;
        this.master = Master.fromJSON(utils, result);
    }

    public static SingleTest fromJSON(BlazeMeterUtils utils, JSONObject obj) {
        return new SingleTest(utils, obj.getString("id"), obj.getString("name"),
                obj.getJSONObject("configuration").getString("type"));
    }
}
