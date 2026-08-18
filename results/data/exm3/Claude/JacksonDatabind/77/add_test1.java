// com/fasterxml/jackson/databind/interop/IllegalTypesCheckTest.java
public void testIllegalTypeInArray() throws Exception
    {
        final String JSON = aposToQuotes(
 "[\n"
+" [ 'com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl',\n"
+"  {\n"
+"    'transletBytecodes' : [ 'WFla' ],\n"
+"    'transletName' : 'm.n',\n"
+"    'outputProperties' : { }\n"
+"  }\n"
+" ]\n"
+"]"
        );
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        try {
            mapper.readValue(JSON, Object[].class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Illegal type");
            verifyException(e, "to deserialize");
            verifyException(e, "prevented for security reasons");
        }
    }