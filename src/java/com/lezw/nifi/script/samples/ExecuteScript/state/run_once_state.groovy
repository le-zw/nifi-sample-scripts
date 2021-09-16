import org.apache.nifi.components.state.*

// 通过 State 来保证只有一个流文件通过(并且会删除上游流文件)

flowFile = session.get()
if(!flowFile) return
StateManager sm = context.stateManager
def oldState = new HashMap<String,String>()
oldState.putAll(sm.getState(Scope.LOCAL).toMap())
if(!oldState.one_shot) {
  // 流文件第一次通过，发送到下游组件并设置 State "one_shot=true"
  session.transfer(flowFile, REL_SUCCESS)
  oldState.one_shot = 'true'
  sm.setState(oldState, Scope.LOCAL)
} else {
  // 如果不是第一次处理流文件，则将流文件删除(注意这会导致上游数据丢失)
  session.remove(flowFile)
}