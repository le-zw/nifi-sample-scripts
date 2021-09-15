import groovy.json.JsonOutput
import org.apache.nifi.components.PropertyDescriptor
import org.apache.nifi.components.ValidationContext
import org.apache.nifi.components.ValidationResult
import org.apache.nifi.processor.*
import org.apache.nifi.processor.exception.ProcessException
import org.apache.nifi.processor.io.OutputStreamCallback

/**
 * 截取文件指定行的指定字符，并且可选是否保留标点符号
 * @author zhongwei.long* @date 2021年08月31日 上午11:27
 * @date 2021年9月15日 14:08:59 星期三
 */
class ExtractCharWithFile implements Processor {

    //properties用于存储processor中属性的配置参数，即针对于processor的用户输入参数
    private List<PropertyDescriptor> properties
    //relationship用于存储这个processor中配置的数据去向关系。
    private Set<Relationship> relationships

    def REL_SUCCESS = new Relationship.Builder()
            .name('success')
            .description('截取文件中指定行的指定位置字符成功')
            .build();

    def REL_FAILURE = new Relationship.Builder()
            .name('failure')
            .description('截取文件中指定行的指定位置字符失败')
            .build();

    def PATH = new PropertyDescriptor.Builder()
            .name('文件路径').description('指定读取的文件路径')
            .required(true)
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build()

    def COMMA_KEEP = new PropertyDescriptor.Builder()
            .name('是否保留逗号').description('截取的字符中存在逗号时选择是否保留，默认保留')
            .required(true)
            .allowableValues('true','false')
            .defaultValue('true')
            .build()

    def BLANK_KEEP = new PropertyDescriptor.Builder()
            .name('是否保留空格').description('截取的字符中存在空格时选择是否保留，默认保留')
            .required(true)
            .allowableValues('true','false')
            .defaultValue('true')
            .build()

    def SEMICOLON_KEEP = new PropertyDescriptor.Builder()
            .name('是否保留分号').description('截取的字符中存在分号时选择是否保留，默认保留')
            .required(true)
            .allowableValues('true','false')
            .defaultValue('true')
            .build()

    def COLON_KEEP = new PropertyDescriptor.Builder()
            .name('是否保留冒号').description('截取的字符中存在冒号时选择是否保留，默认保留')
            .required(true)
            .allowableValues('true','false')
            .defaultValue('true')
            .build()

    def QUESTION_KEEP = new PropertyDescriptor.Builder()
            .name('是否保留问号').description('截取的字符中存在问号时选择是否保留，默认保留')
            .required(true)
            .allowableValues('true','false')
            .defaultValue('true')
            .build()

    def MARK_KEEP = new PropertyDescriptor.Builder()
            .name('是否保留#号').description('截取的字符中存在问号时选择是否保留，默认保留')
            .required(true)
            .allowableValues('true','false')
            .defaultValue('true')
            .build()

    @Override
    void initialize(ProcessorInitializationContext context) {
        def properties = []
        properties.add(PATH)
        properties.add(COMMA_KEEP)
        properties.add(SEMICOLON_KEEP)
        properties.add(COLON_KEEP)
        properties.add(QUESTION_KEEP)
        properties.add(MARK_KEEP)
        properties.add(BLANK_KEEP)
        // 防止多线程ADD
        this.properties = Collections.unmodifiableList(properties)

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
            // 默认自动生成flowfile，不需要上一个组件传递FlowFile来触发
            //def flowFile = session.create()
            if (flowFile == null) {
                return
            }
            def filePath = context.getProperty(PATH)?.evaluateAttributeExpressions()?.getValue()
            def is_comma = context.getProperty(COMMA_KEEP).asBoolean()
            def is_blank = context.getProperty(BLANK_KEEP).asBoolean()
            def is_semicolon = context.getProperty(SEMICOLON_KEEP).asBoolean()
            def is_colon = context.getProperty(COLON_KEEP).asBoolean()
            def is_question = context.getProperty(QUESTION_KEEP).asBoolean()
            def is_mark = context.getProperty(MARK_KEEP).asBoolean()

            def file = new File(filePath)

            def processorProperties = context.getProperties()
            def dynamicAttributes = new LinkedHashMap()
            for (final Map.Entry<PropertyDescriptor, String> entry : processorProperties.entrySet()) {
                PropertyDescriptor property = entry.getKey()
                if (property.isDynamic() && property.isExpressionLanguageSupported()) {
                    //提取用户自定义的动态属性
                    def dynamicValue = context.getProperty(property).evaluateAttributeExpressions().getValue()
//                    dynamicAttributes.put(property.getName(), dynamicValue)
                    //遍历文件行，获取指定字符
                    int line
                    int start
                    int end
                    try {
                        line = property.getName() as Integer
                        start = dynamicValue.toString().split(',')[0] as Integer
                        end = dynamicValue.toString().split(',')[1] as Integer
                    } catch(Exception e) {
                        //跳过自定义属性不是数字的情况
                        continue
                    }
                    def comm = file.readLines().get(line - 1)
                    //如果自定义截取的字符长度超过了该行，则只截取到该行的最后一个字符
                    if (end > comm.length()){end = comm.length()}
                    if (start <= 0 || end <= 0){
                        continue
                    }
                    if (start > end){
                        start ^= end
                        end ^= start
                        start ^= end
                    }
                    def str = comm.substring(start-1, end)
                    if (!is_comma){
                        str = str.replace(',','')
                        str = str.replace('，','')
                    }
                    if (!is_blank){
                        str = str.replace(' ', '')
                    }
                    if (!is_semicolon){
                        str = str.replace(';','')
                        str = str.replace('；','')
                    }
                    if (!is_colon){
                        str = str.replace(':','')
                        str = str.replace('：','')
                    }
                    if (!is_question){
                        str = str.replace('?','')
                        str = str.replace('？','')
                    }
                    if (!is_mark){
                        str = str.replace('#','')
                    }
                    dynamicAttributes.put('R_'.plus(property.getName()), str)
                }
            }


            def content = JsonOutput.toJson(dynamicAttributes)

            flowFile = session.write(flowFile, { outStream ->
                outStream.write(content.getBytes("UTF-8"))
            } as OutputStreamCallback)

            // transfer
            session.transfer(flowFile, REL_SUCCESS)
            session.commit()
        } catch(e) {
//            log.error('Error log : ', e)
            session.transfer(flowFile, REL_SUCCESS)
            session.commit()
        }
    }

    @Override
    Collection<ValidationResult> validate(ValidationContext context) { return null }

    @Override
    PropertyDescriptor getPropertyDescriptor(String name) {
        return null
    }

    @Override
    void onPropertyModified(PropertyDescriptor descriptor, String oldValue, String newValue) { }

    @Override
    List<PropertyDescriptor> getPropertyDescriptors() { return properties}

    @Override
    String getIdentifier() { return 'ExtractCharWithFile-InvokeScriptedProcessor' }

}

processor = new extract_char_with_file()