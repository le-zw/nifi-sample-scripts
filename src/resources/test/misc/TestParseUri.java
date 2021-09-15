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
package com.batchiq.nifi.script.samples.executescript.misc;

import com.batchiq.nifi.script.samples.executescript.BaseScriptTest;
import org.apache.nifi.processors.script.ExecuteScript;
import org.apache.nifi.script.ScriptingComponentUtils;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestParseUri extends BaseScriptTest {

    @Before
    public void setup() throws Exception {
        super.setupExecuteScript();
    }

    /**
     * Parses domain from url
     * @throws Exception
     */
    @Test
    public void testParseUriSimple() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "Groovy");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/misc/parse_uri.groovy");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("input.url", "http://batchiq.com/foobar");
        runner.enqueue("nothing".getBytes(StandardCharsets.UTF_8), attributes);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);
        result.assertAttributeEquals("url.protocol", "http");
        result.assertAttributeEquals("url.host", "batchiq.com");
        result.assertAttributeEquals("url.path", "/foobar");
        result.assertAttributeEquals("input.url", "http://batchiq.com/foobar");
    }

    @Test
    public void testParseUriFail() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "Groovy");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/misc/parse_uri.groovy");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("input.url", "total_bogus");
        runner.enqueue("nothing".getBytes(StandardCharsets.UTF_8), attributes);
        runner.run();

        runner.assertAllFlowFilesTransferred("failure", 1);
        final List<MockFlowFile> failureFlowFiles = runner.getFlowFilesForRelationship("failure");
        MockFlowFile result = failureFlowFiles.get(0);
        result.assertAttributeNotExists("uri.scheme");
        result.assertAttributeNotExists("uri.host");
        result.assertAttributeNotExists("uri.path");
        result.assertAttributeEquals("parse_url.error", "no protocol: total_bogus");
        result.assertAttributeEquals("input.url", "total_bogus");
    }
}
