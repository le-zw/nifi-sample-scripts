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

import org.apache.nifi.processors.script.ExecuteScript;
import org.apache.nifi.properties.NiFiPropertiesLoader;
import org.apache.nifi.script.ScriptingComponentUtils;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.NiFiProperties;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestProperties extends BaseScriptTest {

    /**
     * Demonstrates reading values from nifi.properties
     * @throws Exception
     */
    @Test
    public void testPropertiesJavascript() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        System.setProperty(NiFiProperties.PROPERTIES_FILE_PATH, "src/test/resources/executescript/properties/nifi.properties");
        NiFiPropertiesLoader nifiPropertiesLoader = new NiFiPropertiesLoader();
        NiFiProperties nifiProperties = nifiPropertiesLoader.get();

        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/properties/properties.js");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("property-name", "nifi.version");
        runner.enqueue("sample text".getBytes(StandardCharsets.UTF_8), attributes);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);
        result.assertAttributeEquals("property-value", nifiProperties.getProperty("nifi.version"));
    }


    /**
     * Demonstrates reading values from nifi.properties
     * @throws Exception
     */
    @Test
    public void testPropertiesPython() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        System.setProperty(NiFiProperties.PROPERTIES_FILE_PATH, "src/test/resources/executescript/properties/nifi.properties");
        NiFiPropertiesLoader nifiPropertiesLoader = new NiFiPropertiesLoader();
        NiFiProperties nifiProperties = nifiPropertiesLoader.get();

        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "python");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/properties/properties.py");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("property-name", "nifi.version");
        runner.enqueue("sample text".getBytes(StandardCharsets.UTF_8), attributes);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);
        result.assertAttributeEquals("property-value", nifiProperties.getProperty("nifi.version"));
    }

}
