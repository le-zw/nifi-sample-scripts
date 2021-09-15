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
import org.apache.nifi.script.ScriptingComponentUtils;
import org.apache.nifi.util.LogMessage;
import org.apache.nifi.util.MockComponentLog;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestLog extends BaseScriptTest {

    @Before
    public void setup() throws Exception {
        super.setupExecuteScript();
    }

    /**
     * Demonstrates logging from within scripts
     * @throws Exception
     */
    @Test
    public void testLogJavascript() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/log/log.js");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("greeting", "Hello");
        runner.enqueue("sample text".getBytes(StandardCharsets.UTF_8), attributes);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);

        MockComponentLog log = runner.getLogger();

        List<LogMessage> debugMessages = log.getDebugMessages();
        Assert.assertEquals(1, debugMessages.size());
        Assert.assertTrue(debugMessages.get(0).getMsg().contains("Hello, Debug"));

        List<LogMessage> infoMessages = log.getInfoMessages();
        Assert.assertEquals(1, infoMessages.size());
        Assert.assertTrue(infoMessages.get(0).getMsg().contains("Hello, Info"));

        List<LogMessage> warnMessages = log.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().contains("Hello, Warn"));

        List<LogMessage> errorMessages = log.getErrorMessages();
        Assert.assertEquals(1, errorMessages.size());
        Assert.assertTrue(errorMessages.get(0).getMsg().contains("Hello, Error"));
    }

    /**
     * Demonstrates logging from within scripts
     * @throws Exception
     */
    @Test
    public void testLogPython() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "python");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/log/log.py");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("greeting", "Hello");
        runner.enqueue("sample text".getBytes(StandardCharsets.UTF_8), attributes);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);

        MockComponentLog log = runner.getLogger();

        List<LogMessage> debugMessages = log.getDebugMessages();
        Assert.assertEquals(1, debugMessages.size());
        Assert.assertTrue(debugMessages.get(0).getMsg().contains("Hello, Debug"));

        List<LogMessage> infoMessages = log.getInfoMessages();
        Assert.assertEquals(1, infoMessages.size());
        Assert.assertTrue(infoMessages.get(0).getMsg().contains("Hello, Info"));

        List<LogMessage> warnMessages = log.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().contains("Hello, Warn"));

        List<LogMessage> errorMessages = log.getErrorMessages();
        Assert.assertEquals(1, errorMessages.size());
        Assert.assertTrue(errorMessages.get(0).getMsg().contains("Hello, Error"));
    }

}
