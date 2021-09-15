// 转换流文件(JSON to JSON)

var flowFile = session.get();

if (flowFile !== null) {

    var StreamCallback = Java.type("org.apache.nifi.processor.io.StreamCallback");
    var IOUtils = Java.type("org.apache.commons.io.IOUtils");
    var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");

    flowFile = session.write(flowFile, new StreamCallback(function(inputStream, outputStream) {
        // Read input FlowFile content
        var inputText = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        var inputObj = JSON.parse(inputText);

        // Transform content
        var outputObj = inputObj;
        outputObj.value = outputObj.value * outputObj.value;
        outputObj.message = "Hello";

        // Write output content
        outputStream.write(JSON.stringify(outputObj, null, "\t").getBytes(StandardCharsets.UTF_8));
    }));

    // Finish by transferring the FlowFile to an output relationship
    session.transfer(flowFile, REL_SUCCESS);
}
