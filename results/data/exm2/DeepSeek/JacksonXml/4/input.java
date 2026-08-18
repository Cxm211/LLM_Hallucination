    protected void _serializeXmlNull(JsonGenerator jgen) throws IOException
    {
        // 14-Nov-2016, tatu: As per [dataformat-xml#213], we may have explicitly
        //    configured root name...
        if (jgen instanceof ToXmlGenerator) {
            _initWithRootName((ToXmlGenerator) jgen, ROOT_NAME_FOR_NULL);
        }
        super.serializeValue(jgen, null);
    }

// trigger testcase
public void testDynamicRootName() throws IOException
    {
        String xml;

        ObjectWriter w = _xmlMapper.writer().withRootName("rudy");

        xml = w.writeValueAsString(new StringBean("foo"));
        assertEquals("<rudy><text>foo</text></rudy>", xml);

        xml = w.writeValueAsString(new StringBean(null));
        assertEquals("<rudy><text/></rudy>", xml);

        // and even with null will respect configured root name
        xml = w.writeValueAsString(null);
        assertEquals("<rudy/>", xml);
    }
