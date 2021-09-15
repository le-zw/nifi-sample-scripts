/*
 * Copyright 2016-2017 BatchIQ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.batchiq.nifi.script.samples.executescript;

import com.google.gson.Gson;
import org.apache.nifi.processors.script.ExecuteScript;
import org.apache.nifi.script.ScriptingComponentUtils;
import org.apache.nifi.util.LogMessage;
import org.apache.nifi.util.MockComponentLog;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestContent extends BaseScriptTest {

    private static final Gson gson = new Gson();

    private static class InputObject {
        public int value;
    }

    private static class OutputObject {
        public int value;
        public String message;
    }

    /**
     * Demonstrates transforming the JSON object in an incoming FlowFile to output
     * @throws Exception
     */
    @Test
    public void testTransformJavascript() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/content/transform.js");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        InputObject inputJsonObject = new InputObject();
        inputJsonObject.value = 3;
        String inputJson = gson.toJson(inputJsonObject);

        runner.enqueue(inputJson);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);
        byte[] flowFileBytes = result.toByteArray();
        String outputJson = new String(flowFileBytes);

        OutputObject outputJsonObject = gson.fromJson(outputJson, OutputObject.class);
        Assert.assertEquals(9, outputJsonObject.value);
        Assert.assertEquals("Hello", outputJsonObject.message);
    }

    /**
     * Demonstrates transforming the JSON object in an incoming FlowFile to output
     * @throws Exception
     */
    @Test
    public void testTransformPython() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "python");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/content/transform.py");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        InputObject inputJsonObject = new InputObject();
        inputJsonObject.value = 3;
        String inputJson = gson.toJson(inputJsonObject);

        runner.enqueue(inputJson);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);
        byte[] flowFileBytes = result.toByteArray();
        String outputJson = new String(flowFileBytes);

        OutputObject outputJsonObject = gson.fromJson(outputJson, OutputObject.class);
        Assert.assertEquals(9, outputJsonObject.value);
        Assert.assertEquals("Hello", outputJsonObject.message);
    }

    /**
     * Demonstrates transforming the JSON object in an incoming FlowFile to output
     * @throws Exception
     */
    @Test
    public void testTransformGroovy() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "Groovy");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/content/transform.groovy");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        InputObject inputJsonObject = new InputObject();
        inputJsonObject.value = 3;
        String inputJson = gson.toJson(inputJsonObject);

        runner.enqueue(inputJson);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);
        byte[] flowFileBytes = result.toByteArray();
        String outputJson = new String(flowFileBytes);

        OutputObject outputJsonObject = gson.fromJson(outputJson, OutputObject.class);
        Assert.assertEquals(9, outputJsonObject.value);
        Assert.assertEquals("Hello", outputJsonObject.message);
    }

    /**
     * Demonstrates splitting an array in a single incoming FlowFile into multiple output FlowFiles
     * @throws Exception
     */
    @Test
    public void testSplitJavascript() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/content/split.js");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        String inputContent = "[";
        inputContent += "{ \"color\": \"blue\" },";
        inputContent += "{ \"color\": \"green\" },";
        inputContent += "{ \"color\": \"red\" }";
        inputContent += "]";
        runner.enqueue(inputContent.getBytes(StandardCharsets.UTF_8));
        runner.run();

        MockComponentLog log = runner.getLogger();
        List<LogMessage> infoMessages = log.getInfoMessages();

        runner.assertAllFlowFilesTransferred("success", 3);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile blueFlowFile = successFlowFiles.get(0);
        blueFlowFile.assertAttributeEquals("color", "blue");
        MockFlowFile greenFlowFile = successFlowFiles.get(1);
        greenFlowFile.assertAttributeEquals("color", "green");
        MockFlowFile redFlowFile = successFlowFiles.get(2);
        redFlowFile.assertAttributeEquals("color", "red");
    }

    /**
     * Demonstrates splitting an array in a single incoming FlowFile into multiple output FlowFiles
     * @throws Exception
     */
    @Test
    public void testSplitPython() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "python");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/content/split.py");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        String inputContent = "[";
        inputContent += "{ \"color\": \"blue\" },";
        inputContent += "{ \"color\": \"green\" },";
        inputContent += "{ \"color\": \"red\" }";
        inputContent += "]";
        runner.enqueue(inputContent.getBytes(StandardCharsets.UTF_8));
        runner.run();

        MockComponentLog log = runner.getLogger();
        List<LogMessage> infoMessages = log.getInfoMessages();

        runner.assertAllFlowFilesTransferred("success", 3);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile blueFlowFile = successFlowFiles.get(0);
        blueFlowFile.assertAttributeEquals("color", "blue");
        MockFlowFile greenFlowFile = successFlowFiles.get(1);
        greenFlowFile.assertAttributeEquals("color", "green");
        MockFlowFile redFlowFile = successFlowFiles.get(2);
        redFlowFile.assertAttributeEquals("color", "red");
    }

    /**
     * Customized transformation of FlowFile attributes into JSON output content
     * @throws Exception
     */
    @Test
    public void testAttributesToJsonContentJavascript() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/content/attributesToJSON.js");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("customer", "{\n" +
                "        \"name\": \"Some Customer\",\n" +
                "        \"phone\" : \"+1.234.567.8901\",\n" +
                "        \"email\" : \"some.customer@foobar.com\"\n" +
                "       }");
        attributes.put("product", "{\n" +
                "        \"title\" : \"Avocado Toast\",\n" +
                "        \"price\" : \"10.50\"\n" +
                "       }");
        attributes.put("payment", "{\n" +
                "        \"category\" : \"Credit Card\",\n" +
                "        \"type\" : \"Super Card Titanium\"\n" +
                "       }");
        runner.enqueue("test", attributes);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);
        result.assertContentEquals("{\n" +
                "  \"customer\": {\n" +
                "    \"name\": \"Some Customer\",\n" +
                "    \"phone\": \"+1.234.567.8901\",\n" +
                "    \"email\": \"some.customer@foobar.com\"\n" +
                "  },\n" +
                "  \"product\": {\n" +
                "    \"title\": \"Avocado Toast\",\n" +
                "    \"price\": \"10.50\"\n" +
                "  },\n" +
                "  \"payment\": {\n" +
                "    \"category\": \"Credit Card\",\n" +
                "    \"type\": \"Super Card Titanium\"\n" +
                "  }\n" +
                "}");
    }

}
