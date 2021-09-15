# 在 ExecuteScript 处理 State(UpdateAttribute) 值

from org.apache.nifi.components.state import Scope
from java.util import HashMap

flowFile = session.get()
if flowFile != None:

    # Get current state
    oldState = context.getStateManager().getState(Scope.CLUSTER)
    stateMessage = oldState.get('some-state')

    # Set new state
    newState = HashMap()
    newState.put('some-state', stateMessage + 'bar')
    context.getStateManager().setState(newState, Scope.CLUSTER)

    session.transfer(flowFile, REL_SUCCESS)
