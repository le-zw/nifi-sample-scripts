// date/times 操作

import org.apache.commons.io.IOUtils
import org.apache.nifi.processor.io.StreamCallback

import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

def flowFile = session.get()
if (flowFile == null) {
    return
}

try {
    flowFile = session.write(flowFile, {inputStream, outputStream ->

        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        TimeZone utcZone = TimeZone.getTimeZone("UTC")
        inputDateFormat.setTimeZone(utcZone)

        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        TimeZone outputZone = TimeZone.getTimeZone("America/New_York")
        outputDateFormat.setTimeZone(outputZone)

        String inputDateTime = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        Date utcDate = inputDateFormat.parse(inputDateTime)
        String outputDateTime = outputDateFormat.format(utcDate)
        IOUtils.write(outputDateTime, outputStream, StandardCharsets.UTF_8)

    } as StreamCallback)

    session.transfer(flowFile, REL_SUCCESS)
} catch (Exception ex) {
    flowFile = session.putAttribute(flowFile, "datetime.error", ex.getMessage())
    session.transfer(flowFile, REL_FAILURE)
}
