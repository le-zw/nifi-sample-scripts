import org.apache.nifi.components.state.*

// 模拟 GenerateFlowFile 组件，并且通过 State 保证组件只执行一次

StateManager sm = context.stateManager
def oldState = new HashMap<String,String>()
oldState.putAll(sm.getState(Scope.LOCAL).toMap())
if(!oldState.one_shot) {
  // 流文件第一次通过，创建流文件并设置 State "one_shot=true"
  def flowFile = session.create()
  try {
    String ffContent = context.getProperty('content')?.evaluateAttributeExpressions()?.value
    if(ffContent) {
      flowFile = session.write(flowFile, {outputStream -> 
        outputStream.write(ffContent.bytes)
      } as OutputStreamCallback)
    }
    session.transfer(flowFile, REL_SUCCESS)
  } catch(e) {
    log.error("Couldn't generate FlowFile", e)
    session.remove(flowFile)
  }
  oldState.one_shot = 'true'
  sm.setState(oldState, Scope.LOCAL)
}