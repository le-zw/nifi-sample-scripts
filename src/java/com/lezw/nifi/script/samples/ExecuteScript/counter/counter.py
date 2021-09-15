# 计数器

flowFile = session.get()
if flowFile != None:
    session.adjustCounter("SampleScriptCounter", 1, False)
    session.transfer(flowFile, REL_SUCCESS)

