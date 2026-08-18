// com/fasterxml/jackson/databind/interop/IllegalTypesCheckTest.java
// Inner class for testing superclass hierarchy
    public static class MyTemplatesImplSubclass extends com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl {
        public MyTemplatesImplSubclass() {
            super();
        }
    }

    public void testIssue1599Subclass() throws Exception
    {
        final String JSON = aposToQuotes(
 "{'id': 126,\n"
+" 'obj':[ 'com.fasterxml.jackson.databind.interop.IllegalTypesCheckTest$MyTemplatesImplSubclass',\n"
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
