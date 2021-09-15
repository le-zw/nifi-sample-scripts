def flowFile = session.get();
if (flowFile != null) {
    def test = flowFile.getAttribute("test");
    log.debug(test + ": Debug");
    log.info(test + ": Info");
    log.warn(test + ": Warn");
    log.error(test + ": Error");
    session.transfer(flowFile, REL_SUCCESS);
}