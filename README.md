[![Run Status](https://api.shippable.com/projects/57eb01226fb4bc0e008f0352/badge?branch=master)](https://app.shippable.com/projects/57eb01226fb4bc0e008f0352)

# NiFi Sample Scripts
该仓库整理了一些在 Apache NiFi 中使用的脚本，部分从网上整理过来，部分自己开发。

## Scripts
脚本分成两类，在 ExecuteScript 组件执行的和在 InvokeScriptedProcessor 执行的。

- ExecuteScript

| Topic                           | Groovy                                                       | Javascript                                                   | Python                                                       |
| :------------------------------ | :----------------------------------------------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| 操作 flowfile 属性              | [attributes.groovy](src/java/com/lezw/nifi/script/samples/ExecuteScript/attributes/attributes.groovy)<br />[attributes_filter.groovy](src/java/com/lezw/nifi/script/samples/ExecuteScript/attributes/attributes_filter.groovy)<br />[json_to_Attribute.groovy](src/java/com/lezw/nifi/script/samples/ExecuteScript/attributes/json_to_Attribute.groovy) | [attributes.js](src/java/com/lezw/nifi/script/samples/ExecuteScript/attributes/attributes.js) | [attributes.py](src/java/com/lezw/nifi/script/samples/ExecuteScript/attributes/attributes.py)<br />[update_attribute.py](src/java/com/lezw/nifi/script/samples/ExecuteScript/attributes/update_attribute.py) |
| 日志打印                        | [log.groovy](src/java/com/lezw/nifi/script/samples/ExecuteScript/log/log.groovy) | [log.js](src/java/com/lezw/nifi/script/samples/ExecuteScript/log/log.js) | [log.py](src/java/com/lezw/nifi/script/samples/ExecuteScript/log/log.py) |
| 数据转换                        | [transform.groovy](src/java/com/lezw/nifi/script/samples/ExecuteScript/content/transform.groovy) | [transform.js](src/java/com/lezw/nifi/script/samples/ExecuteScript/content/transform.js) | [transform.py](src/java/com/lezw/nifi/script/samples/ExecuteScript/content/transform.py) |
| 数据分割                        |                                                              | [split.js](src/java/com/lezw/nifi/script/samples/ExecuteScript/content/split.js) | [split.py](src/java/com/lezw/nifi/script/samples/ExecuteScript/content/split.py) |
| 计数器                          |                                                              | [counter.js](src/java/com/lezw/nifi/script/samples/ExecuteScript/counter/counter.js) | [counter.py](src/java/com/lezw/nifi/script/samples/ExecuteScript/counter/counter.py) |
| 读取 nifi.properties            |                                                              | [properties.js](src/java/com/lezw/nifi/script/samples/ExecuteScript/properties/properties.js) | [properties.py](src/java/com/lezw/nifi/script/samples/ExecuteScript/properties/properties.py) |
| 读写 State                      | [state.groovy](src/java/com/lezw/nifi/script/samples/ExecuteScript/state/state.groovy) | [state.js](src/java/com/lezw/nifi/script/samples/ExecuteScript/state/state.js) | [state.py](src/java/com/lezw/nifi/script/samples/ExecuteScript/state/state.py) |
| 解析 URI                        | [parse_uri.groovy](src/java/com/lezw/nifi/script/samples/ExecuteScript/misc/parse_uri.groovy) |                                                              |                                                              |
| XML转JSON                       | [xmlToJson.groovy](src/java/com/lezw/nifi/script/samples/ExecuteScript/content/xml-to-json/xmlToJson.groovy) |                                                              |                                                              |
| 格式化Date/Time                 | [datetime.groovy](src/java/com/lezw/nifi/script/samples/ExecuteScript/content/datetime.groovy) | [datetime.js](src/java/com/lezw/nifi/script/samples/ExecuteScript/content/datetime.js) |                                                              |
| 读写DistributedMapCache缓存数据 | [MapCacheGet](src/java/com/lezw/nifi/script/samples/ExecuteScript/DistributedMapCache/MapCacheGet.groovy)<br />[MapCachePut](/src/java/com/lezw/nifi/script/samples/ExecuteScript/DistributedMapCache/MapCachePut.groovy) |                                                              |                                                              |
| 脚本实现 Jolt 功能              | [Jolt.groovy](src/java/com/lezw/nifi/script/samples/ExecuteScript/Jolt/Jolt_script.groovy) |                                                              |                                                              |
| JSONBuilder示例                 | [JSONBuilder_attribute.groovy](src/java/com/lezw/nifi/script/samples/ExecuteScript/jsonbuilder/jsonbuilder_attribute.groovy)<br />[JSONBuilder_dict.groovy](src/java/com/lezw/nifi/script/samples/ExecuteScript/jsonbuilder/jsonbuilder_dict.groovy) |                                                              |                                                              |
| 字符串前置动态补0               | [string_prefix.groovy](src/java/com/lezw/nifi/script/samples/ExecuteScript/string/string_prefix.groovy) |                                                              |                                                              |

- InvokeScriptedProcessor

| Topic                    | Groovy                                                       |
| :----------------------- | :----------------------------------------------------------- |
| 截取文件指定行的指定字符 | [extract_char_with_file.groovy](src/java/com/lezw/nifi/script/samples/InvokeScripted/extract_char_with_file.groovy) |
| RecordReader             | [RecordReaderProcess.groovy](src/java/com/lezw/nifi/script/samples/InvokeScripted/RecordReaderProcess.groovy) |
| EDI案例                  | [SprintfAttrLinesOnAttributesProcessor.groovy](src/java/com/lezw/nifi/script/samples/InvokeScripted/SprintfAttrLinesOnAttributesProcessor.groovy) |
| UDP报文解析              | [UDP2JSON.groovy](src/java/com/lezw/nifi/script/samples/InvokeScripted/UDPParseScript/UDP2JSON.groovy) |

## License
[Apache License 2.0](/LICENSE)

