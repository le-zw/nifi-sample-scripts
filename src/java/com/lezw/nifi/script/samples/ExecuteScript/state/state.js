// 在 ExecuteScript 处理 State(UpdateAttribute) 值

flowFile = session.get();
if (flowFile != null) {

    var Scope = Java.type("org.apache.nifi.components.state.Scope");
    var HashMap = Java.type("java.util.HashMap");

    // Get current state
    var oldState = context.getStateManager().getState(Scope.CLUSTER);
    var stateMessage = oldState.get("some-state");

    // Set new state
    var newState = new HashMap();
    newState.put("some-state", stateMessage + "bar");
    context.getStateManager().setState(newState, Scope.CLUSTER);

    session.transfer(flowFile, REL_SUCCESS);
}
