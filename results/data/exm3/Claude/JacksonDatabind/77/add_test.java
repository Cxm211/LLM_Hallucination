// com/fasterxml/jackson/databind/interop/IllegalTypesCheckTest.java
public void testIllegalTypeWithNestedObject() throws Exception
    {
        final String JSON = aposToQuotes(
 "{'id': 456,\n"
+" 'nested': {\n"
+"  'obj':[ 'com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl',\n"
+"   {\n"
+"     'transletBytecodes' : [ 'QUJD' ],\n"
+"     'transletName' : 'x.y',\n"
+"     'outputProperties' : { }\n"
+"   }\n"
+"  ]\n"
+" }\n"
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