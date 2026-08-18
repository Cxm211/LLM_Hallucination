// com/fasterxml/jackson/databind/jsontype/TestExternalId.java
public void testExternalTypeIdWithNullAndMultipleFields() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        ExternalBean b;
        b = mapper.readValue(aposToQuotes("{'bean':null,'extType':'vbean','extra':'value'}"),
                ExternalBean.class);
        assertNotNull(b);
        assertNull(b.bean);
    }