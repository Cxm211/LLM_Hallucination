// com/fasterxml/jackson/databind/interop/IllegalTypesCheckTest.java
public void testIssue1599AdditionalBlocked() throws Exception
    {
        final String JSON = aposToQuotes(
 "{'id': 125,\n"
+" 'obj':[ 'com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl',\n"
+"  {\n"
+"    'attribute' : 'test'\n"
+"  }\n"
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