// 转换流文件(JSON to JSON)

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.nifi.processor.io.StreamCallback
import java.nio.charset.StandardCharsets

def flowFile = session.get()
if (flowFile == null) {
    return
}

try {
    flowFile = session.write(flowFile, {inputStream, outputStream ->
        // Read input FlowFile content
        def jsonSlurper = new JsonSlurper()
        def inputObj = jsonSlurper.parse(inputStream)

        // Transform content
        def outputObj = inputObj
        outputObj.value = outputObj.value * outputObj.value
        outputObj.message = "Hello"

        // Write output content
        def json = JsonOutput.toJson(outputObj)
        outputStream.write(json.getBytes(StandardCharsets.UTF_8))
    } as StreamCallback)

    // Finish by transferring the FlowFile to an output relationship
    session.transfer(flowFile, REL_SUCCESS);
} catch (MalformedURLException ex) {
    flowFile = session.putAttribute(flowFile, "parse_url.error", ex.getMessage())
    session.transfer(flowFile, REL_FAILURE)
}
