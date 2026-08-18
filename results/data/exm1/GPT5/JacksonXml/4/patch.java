protected void _serializeXmlNull(JsonGenerator jgen) throws IOException
    {
        if (jgen instanceof ToXmlGenerator) {
            ToXmlGenerator xgen = (ToXmlGenerator) jgen;
            _initWithRootName(xgen, ROOT_NAME_FOR_NULL);
            xgen.writeStartObject();
            xgen.writeEndObject();
            return;
        }
        super.serializeValue(jgen, null);
    }