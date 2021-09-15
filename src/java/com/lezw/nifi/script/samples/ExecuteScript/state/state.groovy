// 在 ExecuteScript 操作 State 值

import org.apache.nifi.components.state.Scope
import java.util.HashMap

def flowFile = session.get();
if (flowFile == null) {
    return;
}
/*
 * State 只在同一组件上生效
 * 共享作用域可以是当前节点上的当前组件(LOCAL)，也可以是集群中多台节点上的当前组件(CLUSTER)
 * 目前没有想到 State 比较好用的场景，仅在流程执行锁机制(保证某个子流程按序处理,当上一条被子流程处理完了下一条数据才能进入子流程)里面用到了
 */
// Get current state
def oldState = context.getStateManager().getState(Scope.CLUSTER) //Scope.LOCAL
def stateMessage = oldState.get("state_lezw");
log.error(stateMessage + ": Error");

// Set new state
def newState = new HashMap();
newState.put("state_lezw", stateMessage + ": demo");

if (oldState.version == -1) {
    // 创建
    context.getStateManager().setState(newState, Scope.CLUSTER);
} else {
    // 替换
    context.getStateManager().replace(oldState, newState, Scope.CLUSTER);
    // 清空 State Map
    //context.getStateManager().clear(Scope.LOCAL)
}

session.transfer(flowFile, REL_SUCCESS);