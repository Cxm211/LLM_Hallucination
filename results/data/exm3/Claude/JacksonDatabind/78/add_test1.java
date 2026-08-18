// com/fasterxml/jackson/databind/interop/IllegalTypesCheckTest.java
public void testIssue1599WithAbstractType() throws Exception
    {
        final String JSON = aposToQuotes(
 "{'id': 126,\n"
+" 'obj':[ 'javax.management.BadAttributeValueExpException',\n"
+"  {\n"
+"    'val' : 'test'\n"
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