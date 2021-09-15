// 计数器

flowFile = session.get();
if (flowFile != null) {
    session.adjustCounter("SampleScriptCounter", 1, false);
    session.transfer(flowFile, REL_SUCCESS);
}
