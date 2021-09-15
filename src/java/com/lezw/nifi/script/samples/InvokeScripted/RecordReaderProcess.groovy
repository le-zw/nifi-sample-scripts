import org.apache.nifi.flowfile.attributes.CoreAttributes
import org.apache.nifi.processor.AbstractProcessor
import org.apache.nifi.processor.ProcessContext
import org.apache.nifi.processor.ProcessSession
import org.apache.nifi.processor.Relationship
import org.apache.nifi.processor.io.StreamCallback
import org.apache.nifi.serialization.*
import org.apache.nifi.serialization.record.*
import org.apache.nifi.schema.access.SchemaNotFoundException
import java.util.concurrent.atomic.AtomicInteger
import org.apache.nifi.logging.ComponentLog

class RecordReaderProcess extends AbstractProcessor {

    def ComponentLog log

    // Properties
    static final PropertyDescriptor RECORD_READER = new PropertyDescriptor.Builder()
            .name("record-reader")
            .displayName("Record Reader")
            .description("Specifies the Controller Service to use for reading incoming data")
            .identifiesControllerService(RecordReaderFactory.class)
            .required(true)
            .build()
    static final PropertyDescriptor RECORD_WRITER = new PropertyDescriptor.Builder()
            .name("record-writer")
            .displayName("Record Writer")
            .description("Specifies the Controller Service to use for writing out the records")
            .identifiesControllerService(RecordSetWriterFactory.class)
            .required(true)
            .build()

    def REL_SUCCESS = new Relationship.Builder().name("success").description('FlowFiles that were successfully processed are routed here').build()
    def REL_FAILURE = new Relationship.Builder().name("failure").description('FlowFiles are routed here if an error occurs during processing').build()

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        def properties = [] as ArrayList
        properties.add(RECORD_READER)
        properties.add(RECORD_WRITER)
        properties as List<PropertyDescriptor>
    }

    @Override
    Set<Relationship> getRelationships() {
        [REL_SUCCESS, REL_FAILURE] as Set<Relationship>
    }

    @Override
    void onTrigger(ProcessContext context, ProcessSession session) {
        def flowFile = session.get()
        if (!flowFile) return

        def readerFactory = context.getProperty(RECORD_READER).asControllerService(RecordReaderFactory)
        def writerFactory = context.getProperty(RECORD_WRITER).asControllerService(RecordSetWriterFactory)

        final Map<String, String> attributes = new HashMap<>()
        final AtomicInteger recordCount = new AtomicInteger()
        final FlowFile original = flowFile
        final Map<String, String> originalAttributes = flowFile.attributes
        try {
            flowFile = session.write(flowFile,  { inStream, outStream ->
                def reader = readerFactory.createRecordReader(originalAttributes, inStream, getLogger())
                try {
                    //在创建Record Writer之前获取第一条记录并对其进行处理。
                    //这样做是为了当Processor更新Record reader的schema，可以及时更新Record Writer的Schema //如果没有记录，那么只需使用Record Reader的Schema创建Writer
                    def firstRecord = reader.nextRecord()
                    if (!firstRecord) {
                        def writeSchema = writerFactory.getSchema(originalAttributes, reader.schema)
                        def writer = writerFactory.createWriter(getLogger(), writeSchema, outStream)
                        try {
                            writer.beginRecordSet()
                            def writeResult = writer.finishRecordSet()
                            attributes['record.count'] = String.valueOf(writeResult.recordCount)
                            attributes[CoreAttributes.MIME_TYPE.key()] = writer.mimeType
                            attributes.putAll(writeResult.attributes)
                            recordCount.set(writeResult.recordCount)
                        } finally {
                            writer.close()
                        }
                        return
                    }

                    /////////////////////////////////////////
                    // TODO Process的第一条记录
                    /////////////////////////////////////////

                    def writeSchema = writerFactory.getSchema(originalAttributes, firstRecord.schema)
                    def writer = writerFactory.createWriter(getLogger(), writeSchema, outStream)
                    try {
                        writer.beginRecordSet()
                        writer.write(firstRecord)
                        def record
                        while (record = reader.nextRecord()) {
                            //////////////////////////////////////////
                            // TODO Process的下一条记录
                            //////////////////////////////////////////
                            writer.write(processed)
                        }

                        def writeResult = writer.finishRecordSet()
                        attributes.put('record.count', String.valueOf(writeResult.recordCount))
                        attributes.put(CoreAttributes.MIME_TYPE.key(), writer.mimeType)
                        attributes.putAll(writeResult.attributes)
                        recordCount.set(writeResult.recordCount)
                    } finally {
                        writer.close()
                    }
                } catch (final SchemaNotFoundException e) {
                    throw new ProcessException(e.localizedMessage, e)
                } catch (final MalformedRecordException e) {
                    throw new ProcessException('Could not parse incoming data', e)
                } finally {
                    reader.close()
                }
            } as StreamCallback)

        } catch (final Exception e) {
            getLogger().error('Failed to process {}; will route to failure', [flowFile, e] as Object[])
            session.transfer(flowFile, REL_FAILURE);
            return;
        }
        flowFile = session.putAllAttributes(flowFile, attributes)
        recordCount.get() ?  session.transfer(flowFile, REL_SUCCESS) : session.remove(flowFile)
        def count = recordCount.get()
        session.adjustCounter('Records Processed', count, false)
        getLogger().info('Successfully converted {} records for {}', [count, flowFile] as Object[])
    }
}

processor = new RecordReaderProcess()