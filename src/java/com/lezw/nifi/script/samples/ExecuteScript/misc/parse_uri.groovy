// 将 URL 解析为流文件属性

import org.apache.commons.lang3.StringUtils
def flowFile = session.get()
if (flowFile == null) {
    return
}

try {
    def url = flowFile.getAttribute("input.url")
    def parsed_url = new URL(url)
    def parsedAttributes = [:]

    def protocol = parsed_url.getProtocol()
    if (StringUtils.isNotBlank(protocol)) {
        parsedAttributes["url.protocol"] = protocol
    }

    def host = parsed_url.getHost()
    if (StringUtils.isNotBlank(host)) {
        parsedAttributes["url.host"] = host
    }

    def port = parsed_url.getPort()
    if (port != -1) {
        parsedAttributes["url.port"] = String.valueOf(port)
    }

    def path = parsed_url.getPath()
    if (StringUtils.isNotBlank(path)) {
        parsedAttributes["url.path"] = path
    }

    def file = parsed_url.getFile()
    if (StringUtils.isNotBlank(file)) {
        parsedAttributes["url.file"] = file
    }

    def query = parsed_url.getQuery()
    if (StringUtils.isNotBlank(query)) {
        parsedAttributes["url.query"] = query
    }

    flowFile = session.putAllAttributes(flowFile, parsedAttributes)
    session.transfer(flowFile, REL_SUCCESS)
} catch (MalformedURLException ex) {
    flowFile = session.putAttribute(flowFile, "parse_url.error", ex.getMessage())
    session.transfer(flowFile, REL_FAILURE)
}
