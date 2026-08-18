protected void _serializeXmlNull(JsonGenerator jgen) throws IOException
    {
        // 14-Nov-2016, tatu: As per [dataformat-xml#213], we may have explicitly
        //    configured root name...
        if (jgen instanceof ToXmlGenerator) {
            ToXmlGenerator xgen = (ToXmlGenerator) jgen;
            // If no root name is already configured, set default for null
            if (xgen.getNextName() == null) {
                _initWithRootName(xgen, ROOT_NAME_FOR_NULL);
            }
        }
        super.serializeValue(jgen, null);
    }