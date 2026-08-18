// buggy function
    protected XmlSerializerProvider(XmlSerializerProvider src) {
        super(src);
        // 21-May-2018, tatu: As per [dataformat-xml#282], should NOT really copy
        //    root name lookup as that may link back to diff version, configuration
        _rootNameLookup = src._rootNameLookup;
    }

// trigger testcase
// com/fasterxml/jackson/dataformat/xml/MapperCopyTest.java::testCopyWith
public void testCopyWith() throws Exception
    {
        XmlMapper xmlMapper = newMapper();
        final ObjectMapper xmlMapperNoAnno = xmlMapper.copy()
                .disable(MapperFeature.USE_ANNOTATIONS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        String xml1 = xmlMapper.writeValueAsString(new Pojo282());
        String xml2 = xmlMapperNoAnno.writeValueAsString(new Pojo282());

        if (!xml1.contains("AnnotatedName")) {
            fail("Should use name 'AnnotatedName', xml = "+xml1);
        }
        if (!xml2.contains("Pojo282")
                || xml2.contains("AnnotatedName")) {
            fail("Should NOT use name 'AnnotatedName' but 'Pojo282', xml = "+xml1);
        }
    }
