import java.text.SimpleDateFormat

/**
[
        {
            "pointName": "Calculation",
            "time": 1622343351,
            "value": "5.752",
            "status": 0,
            "oneId": "",
            "srcName": "ZS_PTS_LOAD_Calculation"
        }
]
将time字段的格式转为datatime
 **/
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets
import groovy.json.JsonOutput
flowFile = session.get()
if(!flowFile) return
flowFile = session.write(flowFile, {inputStream, outputStream ->
    def slurper = new groovy.json.JsonSlurper()
    def text = IOUtils.toString(inputStream, StandardCharsets.UTF_8)//流文件给Text赋值
    def obj = slurper.parseText(text)
SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    for(int i=0;i<obj.size();i++){
        obj.get(i).each {k,v ->
            if (k.equals("time")) {
                String sd = sdf.format(new Date(Long.parseLong(String.valueOf(v+'000'))));
                obj.get(i).put(k,sd)
            }
        }
    }
    outputStream.write(JsonOutput.toJson(obj).getBytes(StandardCharsets.UTF_8))
} as StreamCallback)

session.transfer(flowFile, REL_SUCCESS)