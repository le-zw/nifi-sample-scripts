/*
 * Copyright 2016-2018 BatchIQ
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
package com.batchiq.nifi.script.samples.executescript.content;

import com.batchiq.nifi.script.samples.executescript.BaseScriptTest;
import org.apache.nifi.processors.script.ExecuteScript;
import org.apache.nifi.script.ScriptingComponentUtils;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;


public class TestDateTime extends BaseScriptTest {

    /**
     * Demonstrates applying date/time parsing and formatting.
     * @throws Exception
     */
    @Test
    public void testDateTimeTransformGroovy() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "Groovy");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/content/datetime.groovy");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();
        String inputDateTime = "2018/12/23 10:01:23";

        runner.enqueue(inputDateTime);
        runner.run();

        List<MockFlowFile> failedFlowFiles = runner.getFlowFilesForRelationship(ExecuteScript.REL_FAILURE);
        Assert.assertEquals(0, failedFlowFiles.size());

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);
        byte[] flowFileBytes = result.toByteArray();
        String outputDateTime = new String(flowFileBytes);
        Assert.assertEquals("2018/12/23 05:01:23", outputDateTime);
    }

    /**
     * Demonstrates applying date/time parsing and formatting.
     * @throws Exception
     */
    @Test
    public void testDateTimeTransformJavascript() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/content/datetime.js");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();
        String inputDateTime = "2018/12/23 10:01:23";

        runner.enqueue(inputDateTime);
        runner.run();

        List<MockFlowFile> failedFlowFiles = runner.getFlowFilesForRelationship(ExecuteScript.REL_FAILURE);
        Assert.assertEquals(0, failedFlowFiles.size());

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);
        byte[] flowFileBytes = result.toByteArray();
        String outputDateTime = new String(flowFileBytes);
        Assert.assertEquals("2018/12/23 05:01:23", outputDateTime);
    }

}
