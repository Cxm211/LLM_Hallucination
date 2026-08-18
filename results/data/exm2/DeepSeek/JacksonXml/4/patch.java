protected void _serializeXmlNull(JsonGenerator jgen) throws IOException
{
    // 14-Nov-2016, tatu: As per [dataformat-xml#213], we may have explicitly
    //    configured root name...
    super.serializeValue(jgen, null);
}