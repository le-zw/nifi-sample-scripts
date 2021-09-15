// 流文件属性转JSON

flowFile = session.get();
if (flowFile != null) {
    var OutputStreamCallback = Java.type("org.apache.nifi.processor.io.OutputStreamCallback");
    var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");

    // Get attributes
    var customer = JSON.parse(flowFile.getAttribute("customer"));
    var product = JSON.parse(flowFile.getAttribute("product"));
    var payment = JSON.parse(flowFile.getAttribute("payment"));

    // Combine
    var merged = {
        "customer": customer,
        "product": product,
        "payment": payment
    };

    // Write output content
    flowFile = session.write(flowFile, new OutputStreamCallback(function(outputStream) {
        outputStream.write(JSON.stringify(merged, null, "  ").getBytes(StandardCharsets.UTF_8));
    }));

    session.transfer(flowFile, REL_SUCCESS);
}
