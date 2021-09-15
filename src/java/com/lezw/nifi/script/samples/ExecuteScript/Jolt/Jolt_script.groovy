import org.apache.commons.io.IOUtils;
import org.apache.nifi.processor.io.StreamCallback
import java.nio.charset.StandardCharsets

/**
 * 通过脚本实现 Jolt 的功能
 * @author zhongwei.long
 * @date 2021/8/31 上午11:35
 * @param null
 * @return null
 */

def flowFile = session.get();
if (flowFile == null) {
    return;
}
def slurper = new groovy.json.JsonSlurper()

flowFile = session.write(flowFile,
        { inputStream, outputStream ->
            def text = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
            def obj = slurper.parseText(text)
            def builder = new groovy.json.JsonBuilder()
            builder.call {
                'Range' 5
                'Rating' "${obj.rating.primary.value}"
                'SecondaryRatings' {
                    obj.rating.findAll {it.key != "primary"}.each {k,v ->
                        "$k" {
                            'Id' "$k"
                            'Range' 5
                            'Value' v.value
                        }
                    }
                }
            }
            outputStream.write(builder.toPrettyString().getBytes(StandardCharsets.UTF_8))
        } as StreamCallback)
flowFile = session.putAttribute(flowFile, "filename", flowFile.getAttribute('filename').tokenize('.')[0]+'_translated.json')
session.transfer(flowFile, ExecuteScript.REL_SUCCESS)