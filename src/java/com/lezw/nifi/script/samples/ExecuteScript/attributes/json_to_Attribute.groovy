import org.apache.commons.io.IOUtils
import org.apache.nifi.processor.io.InputStreamCallback
import org.apache.nifi.processor.io.OutputStreamCallback
import java.nio.charset.StandardCharsets

/**
 * JSON中的key-value写到流文件属性
 * @author zhongwei.long
 * @date 2021年08月31日 上午11:24
 */
def flowFile = session.get();
if (flowFile == null) {
    return;
}
def slurper = new groovy.json.JsonSlurper()
def attrs = [:] as Map<String,String>
session.read(flowFile,
        { inputStream ->
            def text = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
            def obj = slurper.parseText(text)
            obj.each {k,v ->
                attrs[k] = v.toString()
            }
        } as InputStreamCallback)

def text = ''
// Cast a closure with an outputStream parameter to OutputStreamCallback
flowFile = session.write(flowFile, {outputStream ->
    outputStream.write(text.getBytes(StandardCharsets.UTF_8))
} as OutputStreamCallback)
flowFile = session.putAllAttributes(flowFile, attrs)
session.transfer(flowFile , REL_SUCCESS)