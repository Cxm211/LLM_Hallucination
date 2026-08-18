// com/fasterxml/jackson/databind/jsontype/TestExternalId.java
public void testExternalTypeIdWithNullAndExtraFields() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        ExternalBean b;
        b = mapper.readValue(aposToQuotes("{'bean':null,'extType':'vbean','extra':123}"),
                ExternalBean.class);
        assertNotNull(b);
        b = mapper.readValue(aposToQuotes("{'extType':'vbean','bean':null,'extra':456}"),
                ExternalBean.class);
        assertNotNull(b);
    }
