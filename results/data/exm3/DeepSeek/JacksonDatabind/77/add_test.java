// com/fasterxml/jackson/databind/interop/IllegalTypesCheckTest.java
// Inner class for testing interface hierarchy
    public static class MyTemplatesInterfaceImpl implements javax.xml.transform.Templates {
        public MyTemplatesInterfaceImpl() {}
        @Override
        public javax.xml.transform.Transformer newTransformer() { return null; }
        @Override
        public java.util.Properties getOutputProperties() { return null; }
    }

    public void testIssue1599Interface() throws Exception
    {
        final String JSON = aposToQuotes(
 "{'id': 125,\n"
+" 'obj':[ 'com.fasterxml.jackson.databind.interop.IllegalTypesCheckTest$MyTemplatesInterfaceImpl',\n"
+"  {}\n"
+" ]\n"
+"}"
        );
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        try {
            mapper.readValue(JSON, Bean1599.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Illegal type");
            verifyException(e, "to deserialize");
            verifyException(e, "prevented for security reasons");
        }
    }
