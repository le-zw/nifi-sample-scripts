/**
 * 属性过滤
 * @author zhongwei.long
 * @date 2021年08月31日 上午11:24
 */
def flowFile = session.get()
if(!flowFile) return
def attrs = new HashMap(flowFile.attributes)
def str = ''
attrs.each { k,v ->
    if (k.equals("http.param.companyid")){
        def companyIds = v.split(",")
        if (companyIds.size() > 0){
            companyIds.each {i ->
                str += "{\"\$oid\":\"" + i + "\"},"
            }
            str = str[0..-2]
        }
    }
}
session.putAttribute(flowFile, "http.param.companyid", str)
session.transfer(flowFile, REL_SUCCESS)