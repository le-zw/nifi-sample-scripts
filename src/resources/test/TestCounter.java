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
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;


public class TestCounter extends BaseScriptTest {

    /**
     * Demonstrates writing to counters
     * @throws Exception
     */
    @Test
    public void testCounterJavascript() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/counter/counter.js");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        runner.enqueue("sample text".getBytes(StandardCharsets.UTF_8));
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        double counterValue = runner.getCounterValue("SampleScriptCounter");
        Assert.assertEquals(1d, counterValue, 0.01d);
    }

    /**
     * Demonstrates writing to counters
     * @throws Exception
     */
    @Test
    public void testCounter() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "python");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/counter/counter.py");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        runner.enqueue("sample text".getBytes(StandardCharsets.UTF_8));
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        double counterValue = runner.getCounterValue("SampleScriptCounter");
        Assert.assertEquals(1d, counterValue, 0.01d);
    }

}
