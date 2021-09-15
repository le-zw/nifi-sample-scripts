import org.apache.commons.io.IOUtils
import org.apache.nifi.distributed.cache.client.DistributedMapCacheClient
import org.apache.nifi.distributed.cache.client.Serializer
import org.apache.nifi.distributed.cache.client.Deserializer
import java.nio.charset.StandardCharsets

/**
 * 更新缓存中的数据 DistributedMapCache
 * @author zhongwei.long
 * @date 2021年9月15日 14:00:02 星期三
 */

def flowFile = session.get();
if (flowFile == null) {
    return;
}
//需要在ExecuteScript增加一条属性：clientServiceId,值设置为DistributedMapCacheClient的uuid

def StringSerializer = {value, out -> out.write(value.getBytes(StandardCharsets.UTF_8))} as Serializer<String>
def StringDeserializer = { bytes -> new String(bytes) } as Deserializer<String>

def myDistClient = clientServiceId.asControllerService(DistributedMapCacheClient)
def result = myDistClient.get('db_code', StringSerializer, StringDeserializer)
def code = (result as Integer) + 1
myDistClient.put('db_code', Integer.toString(code), StringSerializer, StringSerializer)
def new_code = "RY" + String.format("%09d", code)

session.putAttribute(flowFile, "CODE", new_code)
session.transfer(flowFile, ExecuteScript.REL_SUCCESS)