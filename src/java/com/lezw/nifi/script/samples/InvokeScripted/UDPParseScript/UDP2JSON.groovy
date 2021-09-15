import groovy.json.JsonOutput
import org.apache.nifi.components.ValidationResult
import org.apache.nifi.logging.ComponentLog
import org.apache.nifi.processor.ProcessContext
import org.apache.nifi.processor.ProcessSessionFactory
import org.apache.nifi.processor.ProcessorInitializationContext
import org.apache.nifi.processor.Relationship
import org.apache.nifi.processor.exception.ProcessException
import org.apache.nifi.processor.io.StreamCallback

import javax.annotation.processing.Processor
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

// UDP报文按指定规则解析成JSON
class UDP2JSON implements Processor {

    //properties用于存储processor中属性的配置参数，即针对于processor的用户输入参数
    private List<PropertyDescriptor> properties
    //relationship用于存储这个processor中配置的数据去向关系。
    private Set<Relationship> relationships

    def ComponentLog log

    def REL_SUCCESS = new Relationship.Builder()
            .name('success')
            .description('数据解析成功')
            .build();

    def REL_FAILURE = new Relationship.Builder()
            .name('failure')
            .description('数据解析失败')
            .build();
    @Override
    void initialize(ProcessorInitializationContext context) {
        log = context.getLogger()

        def properties = []
        // 防止多线程ADD
        this.properties = Collections.unmodifiableList(properties);

        def relationships = new HashSet<>()
        relationships.add(REL_SUCCESS)
        relationships.add(REL_FAILURE)
        // 防止多线程ADD
        this.relationships = Collections.unmodifiableSet(relationships)
    }

    @Override
    Set<Relationship> getRelationships() { return relationships }

    @Override
    void onTrigger(ProcessContext context, ProcessSessionFactory sessionFactory) throws ProcessException {
        try {
            def session = sessionFactory.createSession()
            def flowFile = session.get()

            //加载动态属性，质量戳的字典映射
            def processorProperties = context.getProperties()
            def attributes = new HashMap()
            for (final Map.Entry<PropertyDescriptor, String> entry : processorProperties.entrySet()) {
                PropertyDescriptor property = entry.getKey();
                if (property.isDynamic() && property.isExpressionLanguageSupported()) {
                    def dynamicValue = context.getProperty(property).evaluateAttributeExpressions().getValue();
                    attributes.put(property.getName(), dynamicValue);
                }
            }
            flowFile = session.write(flowFile,  {inputStream, outputStream ->
                byte[] bytes = new byte[inputStream.available()]
                inputStream.read(bytes)
                //提取数据区，数据区每24个字节为1个HTTagValue
                byte[] data = bytes[12..-1]
                def matchedList = []
                for (i in 0..(data.size()/24)-1){
                    byte[] b = data[i*24..(i+1)*24-1]
                    byte[] tagID = new byte[4]
                    byte[] quality = new byte[4]
                    byte[] tagValue = new byte[8]
                    byte[] timestamp = new byte[8]
                    for (j in 0..7){
                        if (j<=3){
                            tagID[j] = b[j]
                            quality[j] = b[j+4]
                        }
                        tagValue[j] = b[j+8]
                        timestamp[j] = b[j+16]
                    }
                    def matchedLine = new HashMap(4)
                    matchedLine.put('tagID', bytes2Int(tagID))
                    matchedLine.put('qualityStamp', bytes2Quality(quality, attributes))
                    matchedLine.put('tagValue', bytes2Double(tagValue))
                    matchedLine.put('timestamp', toTimeStamp(timestamp))
                    matchedList.add(matchedLine)
                }
                def output = JsonOutput.toJson(matchedList)
                outputStream.write(output.getBytes(StandardCharsets.UTF_8))
            } as StreamCallback)

            // transfer
            session.transfer(flowFile, REL_SUCCESS)
            session.commit()
        } catch(e) {
//            throw new ProcessException(e)
            log.error('Something went wrong', e)
            session.transfer(flowFile, REL_FAILURE)
            session.commit()
        }
    }

    @Override
    Collection<ValidationResult> validate(ValidationContext context) { return null }

    @Override
    PropertyDescriptor getPropertyDescriptor(String name) {
        return null;
    }

    @Override
    void onPropertyModified(PropertyDescriptor descriptor, String oldValue, String newValue) { }

    @Override
    List<PropertyDescriptor> getPropertyDescriptors() { return properties }

    @Override
    String getIdentifier() { return null }


    /**
     * Unix计时开始时间(1970-01-01 00:00:00)与FileTime计时开始时间(1601-01-01 00:00:00)
     * 毫秒数差值
     */
    private final static long UNIX_FILETIME_MILLISECOND_DIFF = 11644473600000L;

    /**
     * FileTime采用100ns为单位的，定义100ns与1ms的倍率
     */
    private final static int MILLISECOND_100NANOSECOND_MULTIPLE = 10000;

    /**
     * 将FileTime转为Date类型
     * @author zhongwei.long
     * @date 2021/4/14 上午10:35
     * @return String Date: yyyy-MM-dd HH:mm:ss
     */
    private String toDate(byte[] bytes) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(bytesToLong(bytes) / MILLISECOND_100NANOSECOND_MULTIPLE - UNIX_FILETIME_MILLISECOND_DIFF as Long);
        return sdf.format(date);
    }

    private Long toTimeStamp(byte[] bytes) {
        return bytesToLong(bytes) / MILLISECOND_100NANOSECOND_MULTIPLE - UNIX_FILETIME_MILLISECOND_DIFF as Long;
    }

    /**
     * 计算测点值
     * @author zhongwei.long
     * @date 2021/4/14 上午9:41
     * @param bytes 8字节测点值,eg: 6F F8 C3 CF 7F 5F 5C 40
     * @return double
     */
    public double bytes2Double(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < bytes.length; i++) {
            value |= ((long) (bytes[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }

    /**
     * 计算测点ID
     * @author zhongwei.long
     * @date 2021/4/13 下午1:25
     * @param bytes 4字节测点ID,eg: 03 00 00 00
     * @return int
     */
    private int bytes2Int(byte[] bytes) {
        int ints = 0;
        for (byte item : bytes) {
            ints += Byte.toUnsignedLong(item);
        }
        return ints;
    }

    /**
     * 计算时间戳
     * @author zhongwei.long
     * @date 2021/4/14 上午10:29
     * @param bytes 8字节时间戳,eg: 00 77 A3 D6 46 30 D7 01
     * @return long FileTime以64位数字表示的值
     */
    private long bytesToLong(byte[] bytes) {
        def result;
        if (bytes.size() > 0){
            def dims = new long[bytes.size()]
            for (i in 0..(bytes.size()-1)) {
                dims[i] = (bytes[i] & 0xFF);
                if (i>0){
                    dims[i] <<= (8 * i);
                }
            }
            result = dims[0];
            for (i in 1..dims.size()-1) {
                result |= dims[i];
            }
        }
        return result;
    }
    /**
     * 计算质量戳
     * @author zhongwei.long
     * @date 2021/4/14 下午4:40
     * @param null
     * @return null
     */
    public String bytes2Quality(byte[] bytes, HashMap map){
        byte[] b = [bytes[0]]
        if (map.containsKey("0x" + b.encodeHex().toString().toUpperCase())){
            return map.get("0x" + b.encodeHex().toString().toUpperCase())
        }else if (map.containsKey("0x" + b.encodeHex().toString().toLowerCase())){
            return map.get("0x" + b.encodeHex().toString().toLowerCase())
        }
        return "0x" + b.encodeHex()
    }

}

processor = new UDP2JSON()