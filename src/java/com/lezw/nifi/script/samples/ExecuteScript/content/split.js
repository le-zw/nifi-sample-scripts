// 将一个输入的流文件分割成多个输出

var parentFlowFile = session.get();

if (parentFlowFile !== null) {

    var InputStreamCallback = Java.type("org.apache.nifi.processor.io.InputStreamCallback");
    var OutputStreamCallback = Java.type("org.apache.nifi.processor.io.OutputStreamCallback");
    var IOUtils = Java.type("org.apache.commons.io.IOUtils");
    var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");

    session.read(parentFlowFile, new InputStreamCallback(function(inputStream) {
        // Read input FlowFile content
        var inputText = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        var inputArray = JSON.parse(inputText);

        // Create FlowFiles for array items
        var splits = [];
        for (var i = 0; i < inputArray.length; i++) {
            var item = inputArray[i];
            log.info(item.color);
            var splitFlowFile = session.create(parentFlowFile);
            splitFlowFile = session.write(splitFlowFile, new OutputStreamCallback(function(outputStream) {
                outputStream.write(JSON.stringify(item, null, "\t").getBytes(StandardCharsets.UTF_8));
            }));
            splitFlowFile = session.putAllAttributes(splitFlowFile, {
                "fragment.index": i.toString(),
                "color": item.color
            });
            splits = splits.concat(splitFlowFile);
        }
        splits.forEach(function (splitFlowFile) {
            session.transfer(splitFlowFile, REL_SUCCESS);
        });
    }));

    session.remove(parentFlowFile);
}
