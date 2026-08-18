// com/fasterxml/jackson/databind/interop/IllegalTypesCheckTest.java
public void testIssue1599_CustomDeserializer() throws Exception
    {
        // Register a custom deserializer for TemplatesImpl
        SimpleModule module = new SimpleModule();
        module.addDeserializer(com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl.class,
            new JsonDeserializer<com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl>() {
                @Override
                public com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    return null; // dummy deserializer
                }
            });
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.enableDefaultTyping();
        final String JSON = aposToQuotes(
 "{'id': 126,\n"
+" 'obj':[ 'com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl',\n"
+"  {\n"
+"    'transletBytecodes' : [ 'AAIAZQ==' ],\n"
+"    'transletName' : 'a.b',\n"
+"    'outputProperties' : { }\n"
+"  }\n"
+" ]\n"
+"}"
        );
        try {
            mapper.readValue(JSON, Bean1599.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Illegal type");
            verifyException(e, "to deserialize");
            verifyException(e, "prevented for security reasons");
        }
    }
