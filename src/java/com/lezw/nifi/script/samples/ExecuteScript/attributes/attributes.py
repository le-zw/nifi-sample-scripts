# 新增属性到流文件

flowFile = session.get()
if flowFile != None:
    # Get attributes
    greeting = flowFile.getAttribute("greeting")
    message = greeting + ", Script!"

    # Set single attribute
    flowFile = session.putAttribute(flowFile, "message", message)

    # Set multiple attributes
    flowFile = session.putAllAttributes(flowFile, {
        "attribute.one": "true",
        "attribute.two": "2"
    })

    session.transfer(flowFile, REL_SUCCESS)