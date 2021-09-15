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

import org.apache.nifi.components.state.Scope;
import org.apache.nifi.components.state.StateManager;
import org.apache.nifi.components.state.StateMap;
import org.apache.nifi.processors.script.ExecuteScript;
import org.apache.nifi.script.ScriptingComponentUtils;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class TestState extends BaseScriptTest{

    /**
     * Demonstrates reading and writing processor state values
     * @throws Exception
     */
    @Test
    public void testStateJavascript() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/state/state.js");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        StateManager stateManager = runner.getStateManager();
        stateManager.clear(Scope.CLUSTER);
        Map<String, String> initialStateValues = new HashMap<>();
        initialStateValues.put("some-state", "foo");
        stateManager.setState(initialStateValues, Scope.CLUSTER);

        runner.enqueue("sample text".getBytes(StandardCharsets.UTF_8));
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        StateMap resultStateValues = stateManager.getState(Scope.CLUSTER);
        Assert.assertEquals("foobar", resultStateValues.get("some-state"));
    }

    /**
     * Demonstrates reading and writing processor state values
     * @throws Exception
     */
    @Test
    public void testStatePython() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(SCRIPT_ENGINE, "python");
        runner.setProperty(ScriptingComponentUtils.SCRIPT_FILE, "src/test/resources/executescript/state/state.py");
        runner.setProperty(ScriptingComponentUtils.MODULES, "src/test/resources/executescript");
        runner.assertValid();

        StateManager stateManager = runner.getStateManager();
        stateManager.clear(Scope.CLUSTER);
        Map<String, String> initialStateValues = new HashMap<>();
        initialStateValues.put("some-state", "foo");
        stateManager.setState(initialStateValues, Scope.CLUSTER);

        runner.enqueue("sample text".getBytes(StandardCharsets.UTF_8));
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        StateMap resultStateValues = stateManager.getState(Scope.CLUSTER);
        Assert.assertEquals("foobar", resultStateValues.get("some-state"));
    }

}
