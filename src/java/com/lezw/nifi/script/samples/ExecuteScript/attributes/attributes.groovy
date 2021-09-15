// 新增属性到流文件

def flowFile = session.get();
if (flowFile != null) {

    // Get attributes
    def greeting = flowFile.getAttribute("greeting")
    def message = greeting + ", Script!"

    // Set single attribute
    flowFile = session.putAttribute(flowFile, "message", message)

    // Set multiple attributes
    flowFile = session.putAllAttributes(flowFile, [
            "attribute.one": "true",
            "attribute.two": "2"
    ])

    session.transfer(flowFile, REL_SUCCESS)
}
