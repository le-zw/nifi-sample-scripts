import org.apache.commons.io.IOUtils
import org.apache.nifi.distributed.cache.client.Deserializer
import org.apache.nifi.distributed.cache.client.DistributedMapCacheClient
import org.apache.nifi.distributed.cache.client.Serializer
import org.apache.nifi.processor.io.StreamCallback

import java.nio.charset.StandardCharsets


/**
 * 获取缓存中的数据 DistributedMapCache
 * @author zhongwei.long
 * @date 2021年9月15日 14:00:02 星期三
 */

def flowFile = session.get();
if (flowFile == null) {
    return;
}
def data = flowFile.getAttribute("data").toString()
session.removeAttribute(flowFile,"data")
def StringSerializer = {value, out -> out.write(value.getBytes(StandardCharsets.UTF_8))} as Serializer<String>
def StringDeserializer = { bytes -> new String(bytes) } as Deserializer<String>
def myDistClient = clientServiceId.asControllerService(DistributedMapCacheClient)

def count = flowFile.getAttribute("executesql.row.count") as Integer
def slurper = new groovy.json.JsonSlurper()
def builder = Integer.toString(count)
flowFile = session.write(flowFile,
        { inputStream, outputStream ->
        
        if(count != 0){
            def text = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
            def obj = slurper.parseText(text)
            builder= "${obj.CODE}"[2..-1]
            def result = myDistClient.get('db_code', StringSerializer, StringDeserializer)
            if(result == '' || (result as Integer) <= (builder as Integer)){
                myDistClient.put('db_code', builder, StringSerializer, StringSerializer)
            }
        }else{
            myDistClient.put('db_code', builder, StringSerializer, StringSerializer)
        }
        outputStream.write(data.getBytes(StandardCharsets.UTF_8))   
        } as StreamCallback)
session.transfer(flowFile, ExecuteScript.REL_SUCCESS)