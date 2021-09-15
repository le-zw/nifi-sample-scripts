import groovy.json.JsonSlurper
import org.apache.nifi.components.PropertyDescriptor
import org.apache.nifi.components.ValidationResult
import org.apache.nifi.logging.ComponentLog
import org.apache.nifi.processor.ProcessContext
import org.apache.nifi.processor.ProcessSessionFactory
import org.apache.nifi.processor.ProcessorInitializationContext
import org.apache.nifi.processor.Relationship
import org.apache.nifi.processor.exception.ProcessException
import org.apache.nifi.processor.io.OutputStreamCallback
import javax.annotation.processing.Processor

// 根据指定的EDI规则处理 flowfile
/**
         *  配置规范格式：
         * [
         *  {
         *      "srcAttrName": "srcAttr",
         *      "tgtAttrName": "tgtAttr",
         *      "tgtStrFormat": "%s",
         *      "blockSize": 10,
         *      "needEscape": true,
         *      "repSRC": "",
         *      "repTGT": "",
         *      "keepSrcAttr": true
         *      "occupiedBit": "",
         *      "isOccupiedBit": true,
         *      "PWI": ${PWI:replaceEmpty('false')},
         *      "readSep": "\\n",
         *      "writeSep": ":",
         *      "specBlockIndex": 5,
         *      "specBlockLFHead": "",
         *      "specBlockLFTail": "'\n",
         *      "specBlockPrefix": "*"
         *  }
         * ];
         *
         * 字段说明:
         * srcAttrName-指定读取源string的流文件属性名
         * tgtAttrName-指定处理后的block输出到的属性名
         * tgtStrFormat-指定输出block的格式；
         * blockSize-指定单个block支持的最大字符数；
         * needEscape-是否转义；
         * repSRC-替换字符时的原字符；
         * repTGT-要替换的字符；
         * occupiedBit-占位符(当block字符数不够blockSize时用占位符补充)
         * isOccupiedBit-是否使用占位符；
         * PWI-是否保留单词的完整性；
         * readSep-读取时block之间的标识符；
         * writeSep-输出时block之间的标识符；
         * specBlockIndex-指定输出到blocks的最大block数量，超过部分字符统一输出speckBlock；
         * specBlockLFHead-指定specBlocks每个block开头的特殊字符；
         * specBlockLFTail-指定specBlocks每个block段尾的特殊字符；
         * specBlockPrefix-在specBlocks的第一个block中添加specBlockPrefix开头；
**/

class SprintfAttrLinesOnAttributesProcessor implements Processor {

    //properties用于存储processor中属性的配置参数，即针对于processor的用户输入参数
    private List<PropertyDescriptor> properties
    //relationship用于存储这个processor中配置的数据去向关系。
    private Set<Relationship> relationships

    def REL_SUCCESS = new Relationship.Builder()
            .name('success')
            .description('FlowFiles that were successfully processed')
            .build();

    def REL_FAILURE = new Relationship.Builder()
            .name('failure')
            .description('FlowFiles that were failed to processed')
            .build();

    def ComponentLog log

    def CONFIG = new PropertyDescriptor.Builder()
            .name('config')
            .defaultValue('[ \n' +
                    '\t{\n' +
                    '\t\t"srcAttrName": "tmp",\n' +
                    '\t\t"tgtAttrName": "tmp.tgt",\n' +
                    '\t\t"tgtStrFormat": "{%s}",\n' +
                    '\t\t"blockSize": 10,\n' +
                    '\t\t"needEscape": true,\n' +
                    '\t\t"repSRC": "",\n' +
                    '\t\t"repTGT": "",\n' +
                    '\t\t"keepSrcAttr": true,\n' +
                    '\t\t"occupiedBit": "*",\n' +
                    '\t\t"isOccupiedBit": true,\n' +
                    '\t\t"PWI": ${PWI:replaceEmpty(\'true\')},\n' +
                    '\t\t"readSep": " ",\n' +
                    '\t\t"writeSep": ":",\n' +
                    '\t\t"specBlockIndex": 4,\n' +
                    '\t\t"specBlockLFHead": "",\n' +
                    '\t\t"specBlockLFTail": "\'\\n",\n' +
                    '\t\t"specBlockPrefix": "*"\n' +
                    '\t}\n' +
                    ']')
            .description('srcAttrName-指定读取源string的流文件属性名\n' +
                    'tgtAttrName-指定处理后的block输出到的属性名\n' +
                    'tgtStrFormat-指定输出block的格式；\n' +
                    'blockSize-指定单个block支持的最大字符数；\n' +
                    'needEscape-是否转义；\n' +
                    'repSRC-替换字符时的原字符；\n' +
                    'repTGT-要替换的字符；\n' +
                    'keepSrcAttr-是否保留原始属性\n' +
                    'occupiedBit-占位符(当block字符数不够blockSize时用占位符补充)\n' +
                    'isOccupiedBit-是否使用占位符；\n' +
                    'PWI-是否保留单词的完整性；\n' +
                    'readSep-读取时block之间的标识符；\n' +
                    'writeSep-输出时block之间的标识符；\n' +
                    'specBlockIndex-指定输出到blocks的最大block数量，超过部分字符统一输出speckBlock；\n' +
                    'specBlockLFHead-指定specBlocks每个block开头的特殊字符；\n' +
                    'specBlockLFTail-指定specBlocks每个block段尾的特殊字符；\n' +
                    'specBlockPrefix-在specBlocks的第一个block中添加specBlockPrefix开头；')
            .required(true)
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build()

    @Override
    void initialize(ProcessorInitializationContext context) {
        log = context.getLogger()

        def properties = []
        properties.add(CONFIG)
        // 防止多线程ADD
        this.properties = Collections.unmodifiableList(properties)

        def relationships = new HashSet<>()
        relationships.add(REL_SUCCESS)
        relationships.add(REL_FAILURE)
        // 防止多线程ADD
        this.relationships = Collections.unmodifiableSet(relationships)
    }

    @Override
    Set<Relationship> getRelationships() {
        return relationships
    }

    @Override
    List<PropertyDescriptor> getPropertyDescriptors() { return properties}

    @Override
    Collection<ValidationResult> validate(ValidationContext context) { return null }

    @Override
    PropertyDescriptor getPropertyDescriptor(String name) { return null }

    @Override
    void onPropertyModified(PropertyDescriptor descriptor, String oldValue, String newValue) { }

    @Override
    String getIdentifier() { return null }

    /**
     *  @param session nifi session
     *  @param ff nifi flowFile
     *  @param attrSpecs 规范
     */
    String generateOutput(Object session, Object ff, Object attrSpecs ) {
        // 规范中的参数声明
        /**
         *  配置规范格式：
         * [
         * 	{
         * 	    "srcAttrName": "srcAttr",
         * 	    "tgtAttrName": "tgtAttr",
         * 		"tgtStrFormat": "%s",
         * 		"blockSize": 10,
         * 		"needEscape": true,
         * 		"repSRC": "",
         * 		"repTGT": "",
         * 	    "keepSrcAttr": true
         * 		"occupiedBit": "",
         * 		"isOccupiedBit": true,
         * 		"PWI": ${PWI:replaceEmpty('false')},
         * 		"readSep": "\\n",
         * 		"writeSep": ":",
         * 		"specBlockIndex": 5,
         * 		"specBlockLFHead": "",
         * 		"specBlockLFTail": "'\n",
         * 		"specBlockPrefix": "*"
         * 	}
         * ];
         *
         * 字段说明:
         * srcAttrName-指定读取源string的流文件属性名
         * tgtAttrName-指定处理后的block输出到的属性名
         * tgtStrFormat-指定输出block的格式；
         * blockSize-指定单个block支持的最大字符数；
         * needEscape-是否转义；
         * repSRC-替换字符时的原字符；
         * repTGT-要替换的字符；
         * occupiedBit-占位符(当block字符数不够blockSize时用占位符补充)
         * isOccupiedBit-是否使用占位符；
         * PWI-是否保留单词的完整性；
         * readSep-读取时block之间的标识符；
         * writeSep-输出时block之间的标识符；
         * specBlockIndex-指定输出到blocks的最大block数量，超过部分字符统一输出speckBlock；
         * specBlockLFHead-指定specBlocks每个block开头的特殊字符；
         * specBlockLFTail-指定specBlocks每个block段尾的特殊字符；
         * specBlockPrefix-在specBlocks的第一个block中添加specBlockPrefix开头；
         */
        // 处理规范
        // log.info("###INFO ### 1.##attrSpecs $attrSpecs")
        for (it in attrSpecs) {
            def srcAttrName = ''
            def srcAttrValue = ''
            def tgtAttrName = ''
            String tgtStrFormat = ''
            boolean needEscape = Boolean.FALSE
            def occupiedBit = ' '
            boolean isOccupiedBit = Boolean.FALSE
            boolean keepSrcAttr = false
            def readSep = ''
            def writeSep = ''
            //Preserve Word Integrity 保留单词的完整性。
            boolean PWI = Boolean.FALSE
            int blockSize = Integer.MAX_VALUE
            //todo:暂无意义的参数
            int maxBlockNums = Integer.MAX_VALUE
            def specKey = ''
            //新增替换字符元字段
            String repSRC = ''
            String repTGT = ''

            //新增4个Block相关特殊处理字段
            int specBlockIndex = Integer.MAX_VALUE
            def specBlockLFHead = ''
            def specBlockLFTail = ''
            def specBlockPrefix = ''

            it.each() {
                // Handle the concreate specification.
                specKey  =  it.key
                // log.info("###INFO ### 2.##attrSpec $it")
                // log.info("###INFO ### 3.##specKey $it.key")
                // log.info("###INFO ### 4.##specValue $it.value")
                switch (specKey) {
                    case 'srcAttrName':
                        srcAttrName = it.value ? it.value : srcAttrName
                        break
                    case 'tgtAttrName':
                        tgtAttrName = it.value ? it.value : tgtAttrName
                        break
                    case 'tgtStrFormat':
                        tgtStrFormat = it.value ? it.value : tgtStrFormat
                        break
                    case 'needEscape':
                        needEscape = it.value ? it.value : needEscape
                        break
                    case 'keepSrcAttr':
                        keepSrcAttr = it.value ? it.value : keepSrcAttr
                        break
                    case 'readSep':
                        readSep = it.value ? it.value : readSep
                        break
                    case 'writeSep':
                        writeSep = it.value ? it.value : writeSep
                        break
                    case 'blockSize':
                        blockSize = it.value ? Integer.valueOf(it.value) : blockSize
                        break
                    case 'maxBlockNums':
                        maxBlockNums = it.value ? Integer.valueOf(it.value) : maxBlockNums
                        break
                    case 'occupiedBit':
                        occupiedBit = it.value ? it.value : occupiedBit
                        break
                    case 'isOccupiedBit':
                        isOccupiedBit = it.value ? it.value : isOccupiedBit
                        break
                    case 'repSRC':
                        repSRC = it.value ? it.value : repSRC
                        break
                    case 'repTGT':
                        repTGT = it.value ? it.value : repTGT
                        break
                    case 'PWI':
                        PWI = it.value ? it.value : PWI
                        break
                    case 'specBlockIndex':
                        specBlockIndex = it.value ? Integer.valueOf(it.value) : specBlockIndex
                        break
                    case 'specBlockLFHead':
                        specBlockLFHead = it.value ? it.value : specBlockLFHead
                        break
                    case 'specBlockLFTail':
                        specBlockLFTail = it.value ? it.value : specBlockLFTail
                        break
                    case 'specBlockPrefix':
                        specBlockPrefix = it.value ? it.value : specBlockPrefix
                        break
                    default:
                        println('功能参数目前未定义!!')
                }
            }

            srcAttrValue = ff.getAttribute("$srcAttrName") ? ff.getAttribute("$srcAttrName") : srcAttrValue

            /*
            1. 根据分割符、block字符大小、是否保留单词完整性将原始数据切分成list
            */
            def blocks = truncateSafe(srcAttrValue, blockSize, readSep, PWI);
//            log.error("#1.blocks##：" + blocks)

            /*
            3. 是否填充占位符
            */
            if (isOccupiedBit){
                blocks = blocks.stream()
                        .map{ isOccupied(it as String, blockSize, occupiedBit) }
                        .collect();
//                log.error("#3.blocks##：" + blocks)
            }

            /*
            4. 是否替换指定字符repSRC -> repTGT
            */
            if ('' != repSRC) {
                blocks = blocks.stream()
                        .map { it.replaceAll(repSRC, repTGT) }
                        .collect();
//                log.error("#4.blocks##：" + blocks)
            }

            //2. 转义字符
            if (needEscape){
                blocks = blocks.stream()
                        .map { escapeStr(it) }
                        .collect();
//                log.error("#2.blocks##：" + blocks)
            }

            /*
            5. 特殊block处理 specBlockIndex
            */
            def specBlocks = dealSpaceBlock(blocks as List<String>, specBlockIndex, specBlockPrefix, specBlockLFHead, specBlockLFTail);
            blocks = blocks.subList(0, specBlockIndex)
//            log.error("#5.blocks##：" + blocks)
//            log.error("#5.specBlocks##：" + specBlocks)

            /*
            6. 指定输出格式
            */
            blocks = blocks.stream().map {formatStr(it as String, tgtStrFormat)}.collect()
            specBlocks = specBlocks.stream()
                    .map {formatStr(it, tgtStrFormat)}
                    .collect();
//            log.error("#6.blocks##：" + blocks)
//            log.error("#6.specBlocks##：" + specBlocks)

            /*
            7. block增加分隔符writeSep
           */
            def blocksValue = blocks.join(writeSep)
            def specBlocksValue = specBlocks.join(writeSep)
//            log.error("#7.blocks##：" + blocksValue)
//            log.error("#7.specBlocks##：" + specBlocksValue)


            //构造输出json
//            def builder = new groovy.json.JsonBuilder()
//            Map<String, Object> overall = new LinkedHashMap();
//            overall.put("blocks", blocksValue);
//            overall.put("specBlocks", specBlocksValue)
//            builder.call(overall)
//            builder.toPrettyString()

            // 输出流文件属性
            //log.debug("###DEBUG### $tgtAttrName = $attrValue")
            session.putAttribute(ff, "$tgtAttrName", blocksValue)
            if (!keepSrcAttr) {
                // 根据keepSrcAttr标记位来判断是否删除原有属性。
                session.removeAttribute(ff, "$srcAttrName") //session.commit()没有commit之前的属性都可以重复使用，提交后remove才生效。
            }
            //处理specBlock
            def sepcBlockAttrName = tgtAttrName + '.specBlocks'
            if (specBlocksValue != '') {
                //去掉最后一个renderSep
                session.putAttribute(ff, sepcBlockAttrName, specBlocksValue)
            }
        }
    }

    @Override
    void onTrigger(ProcessContext context, ProcessSessionFactory sessionFactory) throws ProcessException {
        def session = sessionFactory.createSession()
        def flowFile = session.get()
        if (!flowFile) return
        def jsonSlurper = new JsonSlurper()

        def attrSpecs = context.getProperty('config').evaluateAttributeExpressions(flowFile).getValue()
        try {
            if (!attrSpecs) {
                session.transfer(flowFile, REL_SUCCESS)
                session.commit()
            }
            //log.debug("#ALERT## $attrSpecs")
            attrSpecs = jsonSlurper.parseText(attrSpecs)
            // log.debug("#ALERT## $attrSpecs")
            generateOutput(session, flowFile, attrSpecs)
            session.transfer(flowFile, REL_SUCCESS)
            session.commit()

        } catch (e) {
//            log.error("#异常堆栈##：" + e.getStackTrace())
            flowFile = session.putAttribute(flowFile, 'errorMsg', e.toString())
            flowFile = session.write(flowFile, { outputStream ->
                outputStream.write(e.toString().getBytes('utf-8'))
            } as OutputStreamCallback)
            session.transfer(flowFile, REL_FAILURE)
            session.commit()
        }
    }


    /**
     * 保留单词完整性的前提下分割字符串
     * @param value 字符串
     * @param length 分割的字符长度
     * @praam 字符串单词之间的分隔符
     * @author zhongwei.long
     * @date 2021/3/22 下午3:10
     */
    List<String> safeTruncate(String value,  int length, String readSep) {
        if (length == 0) {
            return value as List;
        }
        if (length >= value.length()) {
            return value as List;
        }
        def list = [];
        boolean endFlag = false;
        while (!endFlag){
            def str = substring(value, 0, length)
            if (str[str.length() - 1] != readSep){
                if (str.contains(readSep)){
                    str = substring(str, 0, str.lastIndexOf(readSep))
                }else {
                    str = substring(value, 0, value.indexOf(readSep)==-1?value.length():value.indexOf(readSep))
                }
            }
            if (str[str.length() - 1] == readSep){
                str = substring(str, 0, str.length() - 2)
            }
            list.add(str)
            if (value == str){
                endFlag = true;
                continue
            }
            value = removeLeft(value, str, true)
            if (value[0] == readSep){
                value = value.substring(1)
            }
        }
        return list
    }

    /**
     *  将字符串按指定长度、指定分隔符切割
     * @param str 原字符串
     * @param fixSize 切割块最大长度
     * @param readSep 分隔符
     * @param PWI Preserve Word Integrity 保留单词的完整性
     * @author zhongwei.long
     * @date 2021/3/19 下午2:16
     */
    List<String> truncateSafe(String str, int fixSize, String readSep, boolean PWI) {
        if (PWI){
            //保留单词完整性
            return safeTruncate(str, fixSize, readSep)
        }
        return getStrList(str, fixSize)
    }

    String truncateStr(String str, int fixSize) {
        int len = (str.length() >= fixSize ? fixSize : str.length())
        String tmpStr = (str ? str[0..(len - 1)] : '')
        return tmpStr
    }

    /**
     * 将字符串按指定步长分割成list
     * @author zhongwei.long
     * @date 2021/3/19 下午1:13
     */
    List<String> getStrList(String str, int length) {
        int size = (int) (str.length() / length);
        if (str.length() % length != 0) {
            size += 1;
        }
        def list = new ArrayList<String>();
        for (index in 0..size) {
            list.add(substring(str, index * length, (index + 1) * length));
        }
        list.removeAll(Collections.singleton(null))
        return list;
    }

    /**
     * 分割字符串，如果开始位置大于字符串长度，返回空
     * @param str 原始字符串
     * @param start 开始位置
     * @param end 结束位置
     * @author zhongwei.long
     * @date 2021/3/19 上午11:36
     */
    static String substring(String str, int start, int end) {
        if (start > str.length())
            return null;
        if (end > str.length()) {
            return str.substring(start, str.length());
        } else {
            return str.substring(start, end);
        }
    }

    /**
     * 移除指定前缀
     * @param value         The input String
     * @param prefix        String to remove on left
     * @param caseSensitive 是否大小写敏感
     * @author zhongwei.long
     * @date 2021/3/19 上午10:46
     */
    static String removeLeft(final String value, final String prefix, final boolean caseSensitive) {
        if (caseSensitive) {
            return value.startsWith(prefix) ? value.substring(prefix.length()) : value;
        }
        return value.toLowerCase().startsWith(prefix.toLowerCase()) ? value.substring(prefix.length()) : value;
    }

    /**
     * 移除指定后缀
     * @param value         The input String
     * @param prefix        String to remove on right
     * @param caseSensitive 是否大小写敏感
     * @author zhongwei.long
     * @date 2021/3/19 上午10:46
     */
    static String removeRight(final String value, final String suffix, final boolean caseSensitive) {
        return endsWith(value, suffix, caseSensitive) ? value
                .substring(0, value.toLowerCase().lastIndexOf(suffix.toLowerCase())) : value;
    }

    /**
     * 判断字符串是否以指定字符结尾
     * @param value         input string
     * @param search        string to search
     * @param caseSensitive 是否大小写敏感
     * @author zhongwei.long
     * @date 2021/3/19 上午10:50
     */
    static boolean endsWith(final String value, final String search, final boolean caseSensitive) {
        int remainingLength = value.length() - search.length();
        if (caseSensitive) {
            return value.indexOf(search, remainingLength) > -1;
        }
        return value.toLowerCase().indexOf(search.toLowerCase(), remainingLength) > -1;
    }

    /**
     * 转义特殊字符
     * 转义规则: 在被转义的字符前添加一个问号.
     * ###需要转义的字符串有：
     *    1. 问号
     *    2. 单引号
     *    3. 冒号
     */
    String escapeStr(String str) {
        String escapeRule = "\\?|'|\\+|:"
        str = str.replaceAll("$escapeRule", '?$0')
        return str
    }

    /**
     *  填充占位符
     * @author zhongwei.long
     * @date 2021/3/19 下午3:06
     */
    String isOccupied(String str, int blockSize, String occupiedBit){
        //使用占位符(当block字符数不够blockSize时用空格补充)
        if (str.length() < blockSize){
            for (i in 0..(blockSize - str.length() - 1)){
                str += occupiedBit;
            }
        }
        return str;
    }

    /*
    * 根据format来格式化输出str
    * 分为以下特殊情况：
    *   - 包含<<RSm.n>>：
    *       其中RS，如果出现就表示需要去除符号以及小数点,remove signal的缩写。
    *       m,即为有效位总数
    *       n,即为小数点位数
    *   - 包含%号特殊字符(%s %d %f)等格式化串：
    *   - 普通字符串，即无特殊字符，输出即为该字符串。
    */
    String formatStr(String str, String format) {
        def tmpStr = ''
        //log.debug("###DEBUG ### 输入formatStr方法的字符串为$str")
        if (!format.contains('<<#')) {
            switch (format) {
                case ~/.*%-?[0-9]*.[0-9]*d/:
                    str = Integer.valueOf(str.trim() ? str.trim() : '0')
                    break
                case ~/.*%-?[0-9|]*.[0-9]*f/:
                    str = Double.valueOf(str.trim() ? str.trim() : '0.0')
                    break
                case ~/.*%-?[0-9|]*s/:
                    String tmpFormat = format.substring(format.indexOf('%') + 1).replaceAll('-', '')
                    // log.debug("###DEBUG $str### 1. tmpFormat $tmpFormat")
                    tmpFormat = tmpFormat[0..tmpFormat.indexOf('s')]
                    // log.debug("###DEBUG $str### 2. tmpFormat $tmpFormat")
                    int strLimit = tmpFormat.matches('[0-9]+') ? Integer.valueOf(tmpFormat) : str.length()
                    // log.debug("###DEBUG $str### 3. strLimit $strLimit")
                    str = (str ? truncateStr(str, strLimit) : '')
                    break
                default:
                    //todo:不做任务处理
                    print(1)
            //log.debug("### #$str 按照format:$format 输出")
            }
            if ('' != format) {
                tmpStr = format.matches('.*[%|\\d|\\.]*[^d|f]+') ? "${str?str:''}" : Double.valueOf(str)
            }
            tmpStr = format ? sprintf("$format", tmpStr) : ''
            // log.debug("### Alert### $tmpStr")
            return tmpStr
        }else {
            //提取<<#RSm.n>>部分的RSm.n参数,正负号±不包含在m(总宽度)当中。
            String specDef = format[(format.indexOf('<<#') + 3)..(format.indexOf('>>') - 1)]

            // 处理包含 <<# 的情况

            boolean keepSignalFlag = true //默认保留符号和小数点.
            int decWidth = 0 // 小数点精确度
            if (specDef.contains('RS')) {
                keepSignalFlag = false
            }
            specDef = specDef.replaceAll('RS', '')
            int totalWidth;
            if (specDef.contains('.')) {
                totalWidth = Integer.valueOf(specDef[0..(specDef.indexOf('.') - 1)])
                decWidth = Integer.valueOf(specDef[(specDef.indexOf('.') + 1)..-1])
            }else {
                totalWidth = Integer.valueOf(specDef[0..-1])
                //入位指定小数点，格式化输出%f时,小数点不出现，但会计算在输出宽度位中，所以此处-1.
                totalWidth -= 1
            }
            // 处理原始str
            def replaceHolder;
            switch (str) {
            // 根据原始str的格式类型，选择相应的format格式输出固定长/宽度的字符串或数值。
                case ~/-?[\d|\.]+/:
                    if (str.contains('.')) {
                        //原始字符串为小数，指定输出格式 为%f类型。
                        totalWidth += 1
                    }
                    if (str.contains('-')) {
                        // 原始字符串为负数，指定输出格式 为%f类型。
                        totalWidth += 1
                    }
                    replaceHolder = "%0${totalWidth}.0${decWidth}f"
                    break
                case ~/.*/:
                    // format = format.replace("<<#.*>>","%${totalWidth}s")
                    replaceHolder = "%${totalWidth}s"
                    str = str ? truncateStr(str, totalWidth) : ''
                    break
                default:
                    //未匹配以上任何场景
                    //log.debug("###DEBUG ### ALERT $str does not match any of the predefined rules!!!")
                    replaceHolder = '%s'
            }
            //设置format
            format = format.replaceAll('<<#.*>>', "$replaceHolder")
            //进行格式化处理
            //log.debug("###DEBUG### ${str}遵循格式[${format}]输出")
            tmpStr = sprintf("$format", replaceHolder.matches('.*[%|\\d|\\.]*[^d|f]+') ? "${str?str:''}" : Double.valueOf(str))

            if (!keepSignalFlag) {
                // 不保留符号（小数点和负号）
                //log.debug("###DEBUG### keepSignalFlag: $keepSignalFlag")
                tmpStr = tmpStr.replaceAll('\\.|-', '')
            }
            //加上suffix，结束字符串处理
            return tmpStr
        }
    }

    List<String> dealSpaceBlock(List<String> blocks, int specBlockIndex, String specBlockPrefix, String specBlockLFHead, String specBlockLFTail){
        def specBlocks = new ArrayList<String>()
        def realBlockNums = blocks.size();
        if (realBlockNums > specBlockIndex){
            for (i in specBlockIndex..realBlockNums-1){
                specBlocks.add(blocks.get(i))
            }
        }
        if (specBlocks.size() > 0){
            // specBlockPrefix: specBlocks的第一个block中添加specBlockPrefix开头；
            specBlocks.set(0, specBlockPrefix + specBlocks.get(0))
            //specBlockLFHead-指定specBlocks每个block开头的特殊字符；
            // pecBlockLFTail-指定specBlocks每个block段尾的特殊字符；
            specBlocks = specBlocks.stream().map { specBlockLFHead + it + specBlockLFTail }.collect();
        }
        return specBlocks;
    }


}

processor = new SprintfAttrLinesOnAttributesProcessor()
