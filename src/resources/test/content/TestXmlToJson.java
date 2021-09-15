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
package com.batchiq.nifi.script.samples.executescript.content;

import com.batchiq.nifi.script.samples.executescript.BaseScriptTest;
import org.apache.nifi.processors.script.ExecuteScript;
import org.apache.nifi.script.ScriptingComponentUtils;
import org.apache.nifi.util.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


/**
 * Demonstrates transforming the JSON object in an incoming FlowFile to output
 * @throws Exception
 */
public class TestXmlToJson extends BaseScriptTest {

    private void runOutputComparisonTest(String inputResource, String expectedResource) throws IOException {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "Groovy");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/content/xml-to-json/xmlToJson.groovy");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.setProperty("prettyPrintJson", "true");
        runner.assertValid();

        Path inputXmlPath = Paths.get(inputResource);
        runner.enqueue(inputXmlPath);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);
        byte[] flowFileBytes = result.toByteArray();

        String actual = new String(flowFileBytes);
        String expected = getFileContentsAsString(expectedResource);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testXmlToJsonSimple() throws Exception {
        String inputResource = "src/test/resources/executescript/content/xml-to-json/simple-input.xml";
        String expectedResource = "src/test/resources/executescript/content/xml-to-json/simple-expected.json";
        runOutputComparisonTest(inputResource, expectedResource);
    }

    @Test
    public void testXmlToJsonNested() throws Exception {
        String inputResource = "src/test/resources/executescript/content/xml-to-json/nested-input.xml";
        String expectedResource = "src/test/resources/executescript/content/xml-to-json/nested-expected.json";
        runOutputComparisonTest(inputResource, expectedResource);
    }

    @Test
    public void testXmlToJsonArray() throws Exception {
        String inputResource = "src/test/resources/executescript/content/xml-to-json/array-input.xml";
        String expectedResource = "src/test/resources/executescript/content/xml-to-json/array-expected.json";
        runOutputComparisonTest(inputResource, expectedResource);
    }

    private String runErrorMessageTest(String inputResouce) throws IOException {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "Groovy");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/content/xml-to-json/xmlToJson.groovy");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.setProperty("prettyPrintJson", "true");
        runner.assertValid();

        Path inputXmlPath = Paths.get(inputResouce);
        runner.enqueue(inputXmlPath);
        runner.run();

        runner.assertAllFlowFilesTransferred("failure", 1);
        final List<MockFlowFile> failureFlowFiles = runner.getFlowFilesForRelationship("failure");
        MockFlowFile result = failureFlowFiles.get(0);
        String errorMessage = result.getAttribute("xmlToJson.error");
        return errorMessage;
    }
    
    @Test
    public void testMalformedXmlErrorMessage() throws IOException {
        String inputResource = "src/test/resources/executescript/content/xml-to-json/malformed.xml";
        String errorMessage = runErrorMessageTest(inputResource);
        Assert.assertTrue(errorMessage.contains("malformed"));
    }

    @Test
    public void testBatchProcessing() throws IOException {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "Groovy");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/content/xml-to-json/xmlToJson.groovy");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.setProperty("batchSize", "7");
        runner.assertValid();

        for (int i = 0; i < 10; i++) {
            String flowFileContent = "<test id=\"" + String.valueOf(i) + "\"/>";
            if (i % 3 == 0) {
                flowFileContent += "<bad><xml>";
            }
            runner.enqueue(flowFileContent);
        }
        
        runner.run(1);

        runner.assertTransferCount("success", 4);
        runner.assertTransferCount("failure", 3);
        runner.assertQueueNotEmpty();
    }
}
