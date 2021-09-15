package ExecuteScript.jsonbuilder

import org.apache.commons.io.IOUtils;
/**
 * JSONBuilder从字典中查找并构建JSON
 * @author zhongwei.long
 * @date 2021年08月31日 上午11:31
 */

import org.apache.nifi.processor.io.StreamCallback

import java.nio.charset.StandardCharsets

class Property {
    String PROPERTYCODE
    String PROPERTYNAME
    String PROPERTYVALUE
}

def flowFile = session.get();
if (flowFile == null) {
    return;
}
def slurper = new groovy.json.JsonSlurper()

def JB = ['PS_ORG_PK':'PS人力组织编码','PS_NC_ORG_CODE':'NC人力组织编码','PS_ORG_NAME':'人力组织名称','ORG_SHORTNAME':'组织简称','PS_ORG_PARENT_CODE':'所属PS上级组织编码','PS_ORG_PARENT_NAME':'所属上级组织名称','BUSINESS_UNITS_CODE':'业务单元编码','BUSINESS_UNITS_NAME':'业务单元名称','BUSINESS_TYPE':'业务类型','ORG_TYPE':'组织类型','ORG_RANK':'组织层级','LAST_UPDATE_USER':'更新人'];

def GL = ['ORG_ESTABLISH_DATE':'组织成立时间','ORG_REVOCATION_DATE':'组织撤销日期','ORG_STATUS':'组织状态','LEADER_CODE':'分管领导工号','LEADER_NAME':'分管领导','HRBP_CODE':'HRBP人员工号','HRBP_NAME':'HRBP人员姓名','ORG_HEAD_CODE':'组织负责人工号','ORG_HEAD_NAME':'组织负责人姓名','TIME_KEEPER_CODE':'考勤员工号'];

def DZ = ['COUNTRY_NAME':'所在国家','COUNTRY_CODE':'所在国家编码','PROVINCE_NAME':'所在省','PROVINCE_CODE':'所在省编码','CITY_NAME':'所在市','CITY_CODE':'所在市编码','COUNTY_NAME':'所在区县','COUNTY_CODE':'所在区县编码'];

def JBXX = []
def GLXX = []
def DZXX = []

flowFile = session.write(flowFile,
        { inputStream, outputStream ->
            def text = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
            def obj = slurper.parseText(text)
            def builder = new groovy.json.JsonBuilder()
            obj.each {k,v ->
                Property p = new Property();
                if(JB.containsKey(k)){
                    p.PROPERTYCODE = k
                    p.PROPERTYNAME = JB.get(k)
                    p.PROPERTYVALUE = v.value
                    JBXX.add(p)
                }
                if(GL.containsKey(k)){
                    p.PROPERTYCODE = k
                    p.PROPERTYNAME = GL.get(k)
                    p.PROPERTYVALUE = v.value
                    GLXX.add(p)
                }
                if(DZ.containsKey(k)){
                    p.PROPERTYCODE = k
                    p.PROPERTYNAME = DZ.get(k)
                    p.PROPERTYVALUE = v.value
                    DZXX.add(p)
                }

            }
            def o1 = ['PROPERTY':JBXX,'SPECIALITYCODE':'JBXX']
            def o2 = ['PROPERTY':GLXX,'SPECIALITYCODE':'GLXX']
            def o3 = ['PROPERTY':DZXX,'SPECIALITYCODE':'DZXX']
            builder.call {
                'CODE' flowFile.getAttribute("CODE")
                'UUID' flowFile.getAttribute("UUID")
                'CODEVALUE' {
                    'SPECIALITY' o1,o2,o3
                }
            }
            outputStream.write(builder.toPrettyString().getBytes(StandardCharsets.UTF_8))
        } as StreamCallback)
session.transfer(flowFile, ExecuteScript.REL_SUCCESS)