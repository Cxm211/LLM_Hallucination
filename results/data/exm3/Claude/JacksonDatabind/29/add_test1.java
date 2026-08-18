// com/fasterxml/jackson/databind/jsontype/TestExternalId.java
public void testExternalTypeIdWithNonNullValue() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(ValueBean.class);
        ExternalBean b;
        b = mapper.readValue(aposToQuotes("{'bean':{'value':42},'extType':'vbean'}"),
                ExternalBean.class);
        assertNotNull(b);
        assertNotNull(b.bean);
        assertTrue(b.bean instanceof ValueBean);
        assertEquals(42, ((ValueBean)b.bean).value);
    }