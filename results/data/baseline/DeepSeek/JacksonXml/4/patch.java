protected void _serializeXmlNull(JsonGenerator jgen) throws IOException
{
    if (jgen instanceof ToXmlGenerator) {
        ToXmlGenerator xmlGen = (ToXmlGenerator) jgen;
        _initWithRootName(xmlGen, ROOT_NAME_FOR_NULL);
        xmlGen.writeStartObject();
        xmlGen.writeFieldName(ROOT_NAME_FOR_NULL);
        xmlGen.writeNull();
        xmlGen.writeEndObject();
    } else {
        super.serializeValue(jgen, null);
    }
}