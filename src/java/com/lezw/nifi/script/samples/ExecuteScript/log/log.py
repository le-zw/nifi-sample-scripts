# Python 记录日志

flowFile = session.get()
if flowFile != None:
    test = flowFile.getAttribute("greeting")
    log.debug(test + ": Debug")
    log.info(test + ": Info")
    log.warn(test + ": Warn")
    log.error(test + ": Error")
    session.transfer(flowFile, REL_SUCCESS)

