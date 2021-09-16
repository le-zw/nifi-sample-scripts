def flowFile = session.get();
if (flowFile != null) {
    def test = flowFile.getAttribute("test");
    log.debug(test + ": Debug");
    log.info(test + ": Info");
    log.warn(test + ": Warn");
    log.error(test + ": Error");
    log.info('Found these things: {} {} {}', ['Hello',1,true] as Object[])
    session.transfer(flowFile, REL_SUCCESS);
}